package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.StackTraceUtils;
import com.reedelk.rest.configuration.listener.ErrorResponse;
import com.reedelk.rest.configuration.listener.Response;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerResponse;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageHttpResponseMapperTest {

    @Mock
    private FlowContext flowContext;
    @Mock
    private HttpServerResponse response;
    @Mock
    private ScriptEngineService scriptEngine;

    @Nested
    @DisplayName("Map message")
    class MapMessage {

        @Nested
        @DisplayName("Output stream is correct")
        class OutputStream {

            @Test
            void shouldOutputStreamFromGivenResponseBodyText() {
                // Given
                String responseBody = "test body";

                MessageHttpResponseMapper mapper = newMapperWithBody(responseBody);
                Message message = MessageBuilder.get().text("a discarded body").build();

                // When
                Publisher<byte[]> actualStream = mapper.map(message, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, "test body");
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldOutputStreamFromGivenResponseScript() throws ScriptException {
                // Given
                String expectedContent = "response content";
                String responseBody = "#[myVariable]";

                MessageHttpResponseMapper mapper = newMapperWithBody(responseBody);
                Message message = MessageBuilder.get().text(expectedContent).build();

                doReturn(new TestScriptExecutionResult(expectedContent))
                        .when(scriptEngine)
                        .evaluate("#[myVariable]", message, flowContext);

                // When
                Publisher<byte[]> actualStream = mapper.map(message, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, expectedContent);
            }

            @Test
            void shouldOutputStreamEmptyForEmptyResponseScript() {
                // Given
                String emptyResponseBody = "";

                MessageHttpResponseMapper mapper = newMapperWithBody(emptyResponseBody);
                Message message = MessageBuilder.get().build();

                // When
                Publisher<byte[]> actualStream = mapper.map(message, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, StringUtils.EMPTY);
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldOutputStreamEmptyForNullResponseScript() {
                // Given
                String nullResponseBody = null;

                MessageHttpResponseMapper mapper = newMapperWithBody(nullResponseBody);
                Message message = MessageBuilder.get().text("something").build();


                // When
                Publisher<byte[]> actualStream = mapper.map(message, response, flowContext);

                // Then
                assertThatStreamIsEmpty(actualStream);
                verifyNoMoreInteractions(scriptEngine);
            }
        }

        @Nested
        @DisplayName("Response status is correct")
        class ResponseStatus {

            @Test
            void shouldSetHttpResponseStatusFromString() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithStatus("201");
                Message message = MessageBuilder.get().text("a body").build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.CREATED);
            }

            @Test
            void shouldSetHttpResponseStatusFromScript() throws ScriptException {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithStatus("#[myStatusCodeVar]");
                Message message = MessageBuilder.get().text("a body").build();

                doReturn(201)
                        .when(scriptEngine)
                        .evaluate("#[myStatusCodeVar]", message, flowContext);

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.CREATED);
            }

            @Test
            void shouldSetDefaultHttpResponseStatusWhenStatusIsNull() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithStatus(null);
                Message message = MessageBuilder.get().text("a body").build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.OK);
            }
        }

        @Nested
        @DisplayName("Content type header is correct")
        class ContentType {

            @Test
            void shouldSetContentTypeHeaderFromMessageContentTypeWhenBodyIsPayload() throws ScriptException {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithBody("#[payload]");
                Message message = MessageBuilder.get().text("my text body").build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldSetContentTypeHeaderWhenBodyIsText() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithBody("my text body");
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldSetContentTypeHeaderWhenBodyIsEmptyText() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithBody("");
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
            }

            @Test
            void shouldNotSetContentTypeHeaderWhenBodyIsNullText() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithBody(null);
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response, never()).addHeader(anyString(), anyString());
                verifyNoMoreInteractions(scriptEngine);
            }
        }

        @Nested
        @DisplayName("Additional headers are correct")
        class AdditionalHeaders {

            @Test
            void shouldAddAdditionalHeaders() {
                // Given
                HttpHeaders initialHeaders = new DefaultHttpHeaders();

                doReturn(initialHeaders).when(response).responseHeaders();

                Map<String,String> headers = new HashMap<>();
                headers.put("header1", "my header 1");
                headers.put("header2", "my header 2");

                MessageHttpResponseMapper mapper = newMapperWithAdditionalHeaders(headers);
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                assertThatContainsHeader(initialHeaders,"header1", "my header 1");
                assertThatContainsHeader(initialHeaders,"header2", "my header 2");
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldOverrideHeaderIfExistsAlreadyCaseInsensitive() {
                // Given
                HttpHeaders initialHeaders = new DefaultHttpHeaders();
                initialHeaders.add(CONTENT_TYPE, "text/html");

                doReturn(initialHeaders).when(response).responseHeaders();

                Map<String,String> headers = new HashMap<>();
                headers.put("coNteNt-TyPe", "new content type");

                MessageHttpResponseMapper mapper = newMapperWithAdditionalHeaders(headers);
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                assertThat(initialHeaders).hasSize(1);
                assertThatContainsHeader(initialHeaders,"coNteNt-TyPe", "new content type");
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldNotAddAnythingAndNotThrowExceptionWhenAdditionalHeadersIsNull() {
                // Given
                HttpHeaders initialHeaders = new DefaultHttpHeaders();

                MessageHttpResponseMapper mapper = newMapperWithAdditionalHeaders(null);
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                assertThat(initialHeaders).isEmpty();
                verifyNoMoreInteractions(scriptEngine);
            }
        }
    }

    @Nested
    @DisplayName("Map exception")
    class MapException {

        @Nested
        @DisplayName("Output stream is correct")
        class OutputStream {

            @Test
            void shouldOutputStreamFromGivenResponseBodyText() {
                // Given
                String expectedBody = "An exception has been thrown";
                Throwable exception = new ESBException("Error while processing JSON");

                MessageHttpResponseMapper mapper = newMapperWithErrorBody(expectedBody);

                // When
                Publisher<byte[]> actualStream = mapper.map(exception, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, expectedBody);
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldOutputStreamFromGivenResponseScript() throws ScriptException {
                // Given
                String expectedContent = "Error while processing JSON";
                String errorResponseBody = "#[error.message]";

                Throwable exception = new ESBException(expectedContent);

                MessageHttpResponseMapper mapper = newMapperWithErrorBody(errorResponseBody);

                doReturn(new TestScriptExecutionResult(expectedContent))
                        .when(scriptEngine)
                        .evaluate(eq("#[error.message]"), eq(flowContext), any(Bindings.class));

                // When
                Publisher<byte[]> actualStream = mapper.map(exception, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, expectedContent);
            }

            @Test
            void shouldOutputStreamFromExceptionStackTraceWithoutCallingScriptEngine() {
                // Given
                String errorResponseBody = "#[error]";

                Throwable exception = new ESBException("Error while processing JSON");

                MessageHttpResponseMapper mapper = newMapperWithErrorBody(errorResponseBody);

                // When
                Publisher<byte[]> actualStream = mapper.map(exception, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, StackTraceUtils.asString(exception));
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldOutputStreamEmptyForEmptyErrorResponseScript() {
                // Given
                String emptyErrorResponseBody = "";
                Throwable exception = new ESBException("Error while processing JSON");
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(emptyErrorResponseBody);

                // When
                Publisher<byte[]> actualStream = mapper.map(exception, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, StringUtils.EMPTY);
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldOutputStreamEmptyForNullErrorResponseScript() {
                // Given
                String emptyErrorResponseBody = null;
                Throwable exception = new ESBException("Error while processing JSON");
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(emptyErrorResponseBody);

                // When
                Publisher<byte[]> actualStream = mapper.map(exception, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, StringUtils.EMPTY);
                verifyNoMoreInteractions(scriptEngine);
            }
        }

        @Nested
        @DisplayName("Response status is correct")
        class ResponseStatus {

            @Test
            void shouldSetHttpResponseStatusFromString() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorStatus("507");
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.INSUFFICIENT_STORAGE);
            }

            @Test
            void shouldSetHttpResponseStatusFromScript() throws ScriptException {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorStatus("#[myStatusCodeVar]");
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(507)
                        .when(scriptEngine)
                        .evaluate(eq("#[myStatusCodeVar]"), eq(flowContext), any(Bindings.class));

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.INSUFFICIENT_STORAGE);
            }

            @Test
            void shouldSetDefaultHttpResponseStatusWhenStatusIsNull() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorStatus(null);
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        }

        @Nested
        @DisplayName("Content type header is correct")
        class ContentType {

            @Test
            void shouldSetContentTypeTextPlainByDefault() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorBody("#[error]");
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldSetContentTypeHeaderWhenBodyIsText() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorBody("my text body");
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldSetContentTypeHeaderWhenBodyIsEmptyText() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorBody("");
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldNotSetContentTypeHeaderWhenBodyIsNullText() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(null);
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response, never()).addHeader(anyString(), anyString());
                verifyNoMoreInteractions(scriptEngine);
            }
        }

        @Nested
        @DisplayName("Additional headers are correct")
        class AdditionalHeaders {

            @Test
            void shouldAddAdditionalHeaders() {
                // Given
                HttpHeaders initialHeaders = new DefaultHttpHeaders();

                doReturn(initialHeaders).when(response).responseHeaders();

                Map<String,String> headers = new HashMap<>();
                headers.put("header1", "my header 1");
                headers.put("header2", "my header 2");

                MessageHttpResponseMapper mapper = newMapperWithErrorAdditionalHeaders(headers);
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                assertThatContainsHeader(initialHeaders,"header1", "my header 1");
                assertThatContainsHeader(initialHeaders,"header2", "my header 2");
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldOverrideHeaderIfExistsAlreadyCaseInsensitive() {
                // Given
                HttpHeaders initialHeaders = new DefaultHttpHeaders();
                initialHeaders.add(CONTENT_TYPE, "text/html");

                doReturn(initialHeaders).when(response).responseHeaders();

                Map<String,String> headers = new HashMap<>();
                headers.put("coNteNt-TyPe", "new content type");

                MessageHttpResponseMapper mapper = newMapperWithErrorAdditionalHeaders(headers);
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                assertThat(initialHeaders).hasSize(1);
                assertThatContainsHeader(initialHeaders,"coNteNt-TyPe", "new content type");
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldNotAddAnythingAndNotThrowExceptionWhenAdditionalHeadersIsNull() {
                // Given
                HttpHeaders initialHeaders = new DefaultHttpHeaders();

                MessageHttpResponseMapper mapper = newMapperWithErrorAdditionalHeaders(null);
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                assertThat(initialHeaders).isEmpty();
                verifyNoMoreInteractions(scriptEngine);
            }
        }
    }


    private void assertThatStreamIs(Publisher<byte[]> actualStream, String expected) {
        List<String> block = Flux.from(actualStream).map(String::new).collectList().block();
        String streamAsString = String.join(StringUtils.EMPTY, block);
        assertThat(streamAsString).isEqualTo(expected);
    }

    private void assertThatStreamIsEmpty(Publisher<byte[]> actualStream) {
        List<byte[]> data = Flux.from(actualStream).collectList().block();
        assertThat(data).isEmpty();
    }

    private void assertThatContainsHeader(HttpHeaders initialHeaders, String headerName, String headerValue) {
        assertThat(initialHeaders.contains(headerName)).isTrue();
        assertThat(initialHeaders.get(headerName)).isEqualTo(headerValue);
    }

    private MessageHttpResponseMapper newMapperWithBody(String responseBody) {
        Response response = new Response();
        response.setBody(responseBody);
        response.setStatus(HttpResponseStatus.OK.codeAsText().toString());
        return new MessageHttpResponseMapper(scriptEngine, response, null);
    }

    private MessageHttpResponseMapper newMapperWithStatus(String responseStatus) {
        Response response = new Response();
        response.setBody("sample body");
        response.setStatus(responseStatus);
        return new MessageHttpResponseMapper(scriptEngine, response, null);
    }

    private MessageHttpResponseMapper newMapperWithAdditionalHeaders(Map<String,String> responseHeaders) {
        Response response = new Response();
        response.setHeaders(responseHeaders);
        response.setBody("sample body");
        response.setStatus(HttpResponseStatus.OK.codeAsText().toString());
        return new MessageHttpResponseMapper(scriptEngine, response, null);
    }

    private MessageHttpResponseMapper newMapperWithErrorBody(String errorBody) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(errorBody);
        return new MessageHttpResponseMapper(scriptEngine, null, errorResponse);
    }

    private MessageHttpResponseMapper newMapperWithErrorStatus(String errorResponseStatus) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody("error body");
        errorResponse.setStatus(errorResponseStatus);
        return new MessageHttpResponseMapper(scriptEngine, null, errorResponse);
    }

    private MessageHttpResponseMapper newMapperWithErrorAdditionalHeaders(Map<String,String> errorResponseHeaders) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHeaders(errorResponseHeaders);
        errorResponse.setBody("error body");
        return new MessageHttpResponseMapper(scriptEngine, null, errorResponse);
    }

    class TestScriptExecutionResult implements ScriptExecutionResult {

        private final Object content;

        TestScriptExecutionResult(Object content) {
            this.content = content;
        }

        @Override
        public Object getObject() {
            return content;
        }

        @Override
        public Bindings getBindings() {
            throw new UnsupportedOperationException();
        }
    }
}