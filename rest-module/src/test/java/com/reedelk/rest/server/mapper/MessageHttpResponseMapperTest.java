package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.HttpHeader;
import com.reedelk.rest.commons.StringUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import io.netty.handler.codec.http.HttpResponseStatus;
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
    }

    @Test
    void shouldOutputStreamFromGivenResponseScript() throws ScriptException {
        // Given
        String expectedContent = "response content";
        String responseBody = "#[payload]";

        MessageHttpResponseMapper mapper = newMapperWithBody(responseBody);
        Message message = MessageBuilder.get().text(expectedContent).build();

        ScriptExecutionResult result = new TestScriptExecutionResult(expectedContent);
        doReturn(result)
                .when(scriptEngine)
                .evaluate("#[payload]", message, flowContext);

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
    }

    @Test
    void shouldOutputStreamEmptyForNullResponseScript() {
        // Given
        String nullResponseBody = null;

        MessageHttpResponseMapper mapper = newMapperWithBody(nullResponseBody);
        Message message = MessageBuilder.get().build();


        // When
        Publisher<byte[]> actualStream = mapper.map(message, response, flowContext);

        // Then
        assertThatStreamIsEmpty(actualStream);
    }

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

    @Test
    void shouldSetContentTypeHeaderFromMessageContentTypeWhenBodyIsPayload() throws ScriptException {
        // Given
        MessageHttpResponseMapper mapper = newMapperWithBody("#[payload]");
        Message message = MessageBuilder.get().text("my text body").build();

        ScriptExecutionResult result = new TestScriptExecutionResult("my text body");
        doReturn(result)
                .when(scriptEngine)
                .evaluate("#[payload]", message, flowContext);

        // When
        mapper.map(message, response, flowContext);

        // Then
        verify(response).addHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT.toString());
    }

    @Test
    void shouldSetContentTypeHeaderWhenBodyIsNotText() {
        // Given
        MessageHttpResponseMapper mapper = newMapperWithBody("my text body");
        Message message = MessageBuilder.get().build();

        // When
        mapper.map(message, response, flowContext);

        // Then
        verify(response).addHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT.toString());
    }

    @Test
    void shouldNotSetContentTypeHeaderWhenBodyIsEmptyText() {
        // Given
        MessageHttpResponseMapper mapper = newMapperWithBody("");
        Message message = MessageBuilder.get().build();

        // When
        mapper.map(message, response, flowContext);

        // Then
        verify(response, never()).addHeader(anyString(), anyString());
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

    private MessageHttpResponseMapper newMapperWithStatus(String responseStatus) {
        return new MessageHttpResponseMapper(
                scriptEngine, "sample body", responseStatus,
                new HashMap<>(),null,null);
    }

    private MessageHttpResponseMapper newMapperWithBody(String responseBody) {
        return new MessageHttpResponseMapper(
                scriptEngine, responseBody, HttpResponseStatus.OK.codeAsText().toString(),
                new HashMap<>(),null,null);
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