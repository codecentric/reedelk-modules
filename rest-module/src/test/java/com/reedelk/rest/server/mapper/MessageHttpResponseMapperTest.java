package com.reedelk.rest.server.mapper;

import com.reedelk.rest.configuration.listener.ErrorResponse;
import com.reedelk.rest.configuration.listener.Response;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.script.dynamicmap.DynamicStringMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicInteger;
import com.reedelk.runtime.api.service.ScriptEngineService;
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

import java.util.List;
import java.util.Optional;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.runtime.api.commons.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageHttpResponseMapperTest {

    private final long testModuleId = 10L;

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
        @DisplayName("Response status is correct")
        class ResponseStatus {

            @Test
            void shouldSetHttpResponseStatusFromString() {
                // Given
                DynamicInteger status = DynamicInteger.from("201", testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithStatus(status);
                Message message = MessageBuilder.get().text("a body").build();

                doReturn(Optional.of(201))
                        .when(scriptEngine).evaluate(status, flowContext, message);

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.CREATED);
            }

            @Test
            void shouldSetHttpResponseStatusFromScript() {
                // Given
                DynamicInteger status = DynamicInteger.from("#[myStatusCodeVar]", testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithStatus(status);
                Message message = MessageBuilder.get().text("a body").build();

                doReturn(Optional.of(201))
                        .when(scriptEngine)
                        .evaluate(status, flowContext, message);

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.CREATED);
            }

            @Test
            void shouldSetDefaultHttpResponseStatusWhenStatusIsNull() {
                // Given
                DynamicInteger status = null;
                MessageHttpResponseMapper mapper = newMapperWithStatus(status);
                Message message = MessageBuilder.get().text("a body").build();

                doReturn(Optional.empty())
                        .when(scriptEngine).evaluate(status, flowContext, message);

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
            void shouldSetContentTypeHeaderFromMessageContentTypeWhenBodyIsPayload() {
                // Given
                DynamicByteArray body = DynamicByteArray.from("#[message.payload()]", testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithBody(body);
                Message message = MessageBuilder.get().text("my text body").build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldNotSetContentTypeHeaderWhenBodyIsEmptyText() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithBody(DynamicByteArray.from("", testModuleId));
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response, never()).addHeader(anyString(), anyString());
            }

            @Test
            void shouldNotSetContentTypeHeaderWhenBodyIsNull() {
                // Given
                DynamicByteArray body = null;
                MessageHttpResponseMapper mapper = newMapperWithBody(body);
                Message message = MessageBuilder.get().build();

                // When
                mapper.map(message, response, flowContext);

                // Then
                verify(response, never()).addHeader(anyString(), anyString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldNotSetContentTypeHeaderWhenBodyIsNullBody() {
                // Given
                DynamicByteArray body = DynamicByteArray.from(null, testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithBody(body);
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

                DynamicStringMap headers = DynamicStringMap.empty();
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

                DynamicStringMap headers = DynamicStringMap.empty();
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
        @DisplayName("Response status is correct")
        class ResponseStatus {

            @Test
            void shouldSetHttpResponseStatusFromString() {
                // Given
                DynamicInteger status = DynamicInteger.from("#[507]", testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithErrorStatus(status);
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(Optional.of(507))
                        .when(scriptEngine).evaluate(status, flowContext, exception);

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).status(HttpResponseStatus.INSUFFICIENT_STORAGE);
            }

            @Test
            void shouldSetHttpResponseStatusFromScript() {
                // Given
                DynamicInteger errorStatus = DynamicInteger.from("#[myStatusCodeVar]", testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithErrorStatus(errorStatus);
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(Optional.of(507))
                        .when(scriptEngine).evaluate(errorStatus, flowContext, exception);

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
                DynamicByteArray errorBody = DynamicByteArray.from("#[error]", testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(errorBody);
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(Optional.empty())
                        .when(scriptEngine).evaluate(null, flowContext, exception);

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldSetContentTypeHeaderWhenBodyIsText() {
                // Given
                DynamicByteArray errorBody = DynamicByteArray.from("my text body", testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(errorBody);
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(Optional.empty())
                        .when(scriptEngine).evaluate(null, flowContext, exception);

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
            }

            @Test
            void shouldSetContentTypeHeaderWhenBodyIsEmptyText() {
                // Given
                DynamicByteArray errorBody = DynamicByteArray.from("", testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(errorBody);
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(Optional.empty())
                        .when(scriptEngine).evaluate(null, flowContext, exception);

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response).addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldNotSetContentTypeHeaderWhenBodyIsNullText() {
                // Given
                DynamicByteArray errorBody = DynamicByteArray.from(null, testModuleId);
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(errorBody);
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(Optional.empty())
                        .when(scriptEngine).evaluate(null, flowContext, exception);

                // When
                mapper.map(exception, response, flowContext);

                // Then
                verify(response, never()).addHeader(anyString(), anyString());
                verifyNoMoreInteractions(scriptEngine);
            }

            @Test
            void shouldNotSetContentTypeHeaderWhenBodyIsNull() {
                // Given
                MessageHttpResponseMapper mapper = newMapperWithErrorBody(null);
                Throwable exception = new ESBException("Error while processing JSON");

                doReturn(Optional.empty())
                        .when(scriptEngine).evaluate(null, flowContext, exception);

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

                DynamicStringMap headers = DynamicStringMap.empty();
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

                DynamicStringMap headers = DynamicStringMap.empty();
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
                verify(scriptEngine, never()).evaluate(any(DynamicStringMap.class), any(FlowContext.class), any(Message.class));
            }
        }
    }


    private void assertThatStreamIs(Publisher<byte[]> actualStream, String expected) {
        List<String> block = Flux.from(actualStream).map(String::new).collectList().block();
        String streamAsString = String.join(EMPTY, block);
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

    private MessageHttpResponseMapper newMapperWithBody(DynamicByteArray responseBody) {
        DynamicInteger statusValue = DynamicInteger.from(HttpResponseStatus.OK.codeAsText().toString(), testModuleId);
        Response response = new Response();
        response.setBody(responseBody);
        response.setStatus(statusValue);

        doReturn(Optional.of(200))
                .when(scriptEngine).evaluate(eq(statusValue), any(FlowContext.class), any(Message.class));

        return new MessageHttpResponseMapper(scriptEngine, response, null);
    }

    private MessageHttpResponseMapper newMapperWithStatus(DynamicInteger responseStatus) {
        String bodyContent = "sample body";
        DynamicByteArray value = DynamicByteArray.from(bodyContent, testModuleId);
        Response response = new Response();
        response.setBody(value);
        response.setStatus(responseStatus);
        return new MessageHttpResponseMapper(scriptEngine, response, null);
    }

    private MessageHttpResponseMapper newMapperWithAdditionalHeaders(DynamicStringMap responseHeaders) {
        String bodyContent = "sample body";
        DynamicByteArray bodyValue = DynamicByteArray.from(bodyContent, testModuleId);
        DynamicInteger statusValue = DynamicInteger.from(HttpResponseStatus.OK.codeAsText().toString(), testModuleId);
        Response response = new Response();
        response.setHeaders(responseHeaders);
        response.setBody(bodyValue);
        response.setStatus(statusValue);

        doReturn(Optional.of(200))
                .when(scriptEngine).evaluate(eq(statusValue), any(FlowContext.class), any(Message.class));

        return new MessageHttpResponseMapper(scriptEngine, response, null);
    }

    private MessageHttpResponseMapper newMapperWithErrorBody(DynamicByteArray errorBody) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(errorBody);
        return new MessageHttpResponseMapper(scriptEngine, null, errorResponse);
    }

    private MessageHttpResponseMapper newMapperWithErrorStatus(DynamicInteger errorResponseStatus) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(DynamicByteArray.from("error body", testModuleId));
        errorResponse.setStatus(errorResponseStatus);
        return new MessageHttpResponseMapper(scriptEngine, null, errorResponse);
    }

    private MessageHttpResponseMapper newMapperWithErrorAdditionalHeaders(DynamicStringMap errorResponseHeaders) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHeaders(errorResponseHeaders);
        errorResponse.setBody(DynamicByteArray.from("error body", testModuleId));
        return new MessageHttpResponseMapper(scriptEngine, null, errorResponse);
    }
}