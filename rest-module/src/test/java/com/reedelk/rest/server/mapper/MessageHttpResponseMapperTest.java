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
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicValue;
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
import java.util.List;

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
                DynamicValue responseBody = DynamicValue.from("test body");

                MessageHttpResponseMapper mapper = newMapperWithBody(responseBody);
                Message message = MessageBuilder.get().text("a discarded body").build();

                doReturn("test body")
                        .when(scriptEngine)
                        .evaluate(responseBody, message, flowContext);

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
                DynamicValue responseBody = DynamicValue.from("#[myVariable]");

                MessageHttpResponseMapper mapper = newMapperWithBody(responseBody);
                Message message = MessageBuilder.get().text(expectedContent).build();

                doReturn(expectedContent)
                        .when(scriptEngine)
                        .evaluate(DynamicValue.from("#[myVariable]"), message, flowContext);

                // When
                Publisher<byte[]> actualStream = mapper.map(message, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, expectedContent);
            }

            @Test
            void shouldOutputStreamEmptyForEmptyResponseScript() {
                // Given
                DynamicValue emptyResponseBody = DynamicValue.from("");

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
                DynamicValue nullResponseBody = null;

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
                DynamicValue status = DynamicValue.from("201");
                MessageHttpResponseMapper mapper = newMapperWithStatus(status);
                Message message = MessageBuilder.get().text("a body").build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.CREATED);
            }

            @Test
            void shouldSetHttpResponseStatusFromScript() throws ScriptException {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithStatus(DynamicValue.from("#[myStatusCodeVar]"));
                Message message = MessageBuilder.get().text("a body").build();

                doReturn(201)
                        .when(scriptEngine)
                        .evaluate(DynamicValue.from("#[myStatusCodeVar]"), message, flowContext);

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
                MessageHttpResponseMapper mapper = newMapperWithBody(DynamicValue.from("#[payload]"));
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
                DynamicValue body = DynamicValue.from("my text body");
                MessageHttpResponseMapper mapper = newMapperWithBody(body);
                Message message = MessageBuilder.get().build();

                doReturn("my text body")
                        .when(scriptEngine)
                        .evaluate(body, message, flowContext);

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldSetContentTypeHeaderWhenBodyIsEmptyText() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithBody(DynamicValue.from(""));
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

                DynamicMap<String> headers = DynamicMap.empty();
                headers.put("header1", "my header 1");
                headers.put("header2", "my header 2");

                MessageHttpResponseMapper mapper = newMapperWithAdditionalHeaders(headers);
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                assertThatContainsHeader(initialHeaders, "header1", "my header 1");
                assertThatContainsHeader(initialHeaders, "header2", "my header 2");
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldOverrideHeaderIfExistsAlreadyCaseInsensitive() {
                // Given
                HttpHeaders initialHeaders = new DefaultHttpHeaders();
                initialHeaders.add(CONTENT_TYPE, "text/html");

                doReturn(initialHeaders).when(response).responseHeaders();

                DynamicMap<String> headers = DynamicMap.empty();
                headers.put("coNteNt-TyPe", "new content type");

                MessageHttpResponseMapper mapper = newMapperWithAdditionalHeaders(headers);
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                assertThat(initialHeaders).hasSize(1);
                assertThatContainsHeader(initialHeaders, "coNteNt-TyPe", "new content type");
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
                DynamicValue bodyValue = DynamicValue.from(expectedBody);
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(expectedBody)
                        .when(scriptEngine)
                        .evaluate(bodyValue, exception, flowContext);

                MessageHttpResponseMapper mapper = newMapperWithErrorBody(bodyValue);

                // When
                Publisher<byte[]> actualStream = mapper.map(exception, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, expectedBody);
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldOutputStreamFromGivenResponseScript() {
                // Given
                String expectedContent = "Error while processing JSON";
                DynamicValue errorResponseBody = DynamicValue.from("#[error.message]");

                Throwable exception = new ESBException(expectedContent);

                MessageHttpResponseMapper mapper = newMapperWithErrorBody(errorResponseBody);

                doReturn(expectedContent)
                        .when(scriptEngine)
                        .evaluate(eq(DynamicValue.from("#[error.message]")), eq(exception), eq(flowContext));

                // When
                Publisher<byte[]> actualStream = mapper.map(exception, response, flowContext);

                // Then
                assertThatStreamIs(actualStream, expectedContent);
            }

            @Test
            void shouldOutputStreamFromExceptionStackTraceWithoutCallingScriptEngine() {
                // Given
                DynamicValue errorResponseBody = DynamicValue.from("#[error]");

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
                DynamicValue emptyErrorResponseBody = DynamicValue.from("");
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
                DynamicValue emptyErrorResponseBody = null;
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
                DynamicValue status = DynamicValue.from("#[507]");
                MessageHttpResponseMapper mapper = newMapperWithErrorStatus(status);
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(507)
                        .when(scriptEngine).evaluate(status, exception, flowContext);
                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.INSUFFICIENT_STORAGE);
            }

            @Test
            void shouldSetHttpResponseStatusFromScript() throws ScriptException {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorStatus(DynamicValue.from("#[myStatusCodeVar]"));
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(507)
                        .when(scriptEngine)
                        .evaluate(eq(DynamicValue.from("#[myStatusCodeVar]")), eq(exception), eq(flowContext));

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
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(DynamicValue.from("#[error]"));
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
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(DynamicValue.from("my text body"));
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
            }

            @Test
            void shouldSetContentTypeHeaderWhenBodyIsEmptyText() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(DynamicValue.from(""));
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

                DynamicMap<String> headers = DynamicMap.empty();
                headers.put("header1", "my header 1");
                headers.put("header2", "my header 2");

                MessageHttpResponseMapper mapper = newMapperWithErrorAdditionalHeaders(headers);
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                assertThatContainsHeader(initialHeaders, "header1", "my header 1");
                assertThatContainsHeader(initialHeaders, "header2", "my header 2");
            }

            @Test
            void shouldOverrideHeaderIfExistsAlreadyCaseInsensitive() {
                // Given
                HttpHeaders initialHeaders = new DefaultHttpHeaders();
                initialHeaders.add(CONTENT_TYPE, "text/html");

                doReturn(initialHeaders).when(response).responseHeaders();

                DynamicMap<String> headers = DynamicMap.empty();
                headers.put("coNteNt-TyPe", "new content type");

                MessageHttpResponseMapper mapper = newMapperWithErrorAdditionalHeaders(headers);
                Throwable exception = new ESBException("Error while processing JSON");

                // When
                mapper.map(exception, response, flowContext);

                // Then
                assertThat(initialHeaders).hasSize(1);
                assertThatContainsHeader(initialHeaders, "coNteNt-TyPe", "new content type");
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
                verify(scriptEngine, never()).evaluate(any(Message.class), any(FlowContext.class), any(DynamicMap.class));
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

    private MessageHttpResponseMapper newMapperWithBody(DynamicValue responseBody) {
        DynamicValue status = DynamicValue.from(HttpResponseStatus.OK.codeAsText().toString());
        Response response = new Response();
        response.setBody(responseBody);
        response.setStatus(status);
        return new MessageHttpResponseMapper(scriptEngine, response, null);
    }

    private MessageHttpResponseMapper newMapperWithStatus(DynamicValue responseStatus) {
        DynamicValue value = DynamicValue.from("sample body");
        Response response = new Response();
        response.setBody(value);
        response.setStatus(responseStatus);

        doReturn("sample body")
                .when(scriptEngine)
                .evaluate(eq(value), any(Message.class), eq(flowContext));

        return new MessageHttpResponseMapper(scriptEngine, response, null);
    }

    private MessageHttpResponseMapper newMapperWithAdditionalHeaders(DynamicMap<String> responseHeaders) {
        DynamicValue bodyValue = DynamicValue.from("sample body");
        DynamicValue statusValue = DynamicValue.from(HttpResponseStatus.OK.codeAsText().toString());
        Response response = new Response();
        response.setHeaders(responseHeaders);
        response.setBody(bodyValue);
        response.setStatus(statusValue);

        doReturn("sample body")
                .when(scriptEngine)
                .evaluate(eq(bodyValue), any(Message.class), eq(flowContext));

        return new MessageHttpResponseMapper(scriptEngine, response, null);
    }

    private MessageHttpResponseMapper newMapperWithErrorBody(DynamicValue errorBody) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(errorBody);
        return new MessageHttpResponseMapper(scriptEngine, null, errorResponse);
    }

    private MessageHttpResponseMapper newMapperWithErrorStatus(DynamicValue errorResponseStatus) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(DynamicValue.from("error body"));
        errorResponse.setStatus(errorResponseStatus);
        return new MessageHttpResponseMapper(scriptEngine, null, errorResponse);
    }

    private MessageHttpResponseMapper newMapperWithErrorAdditionalHeaders(DynamicMap<String> errorResponseHeaders) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHeaders(errorResponseHeaders);
        errorResponse.setBody(DynamicValue.from("error body"));
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