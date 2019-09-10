package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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

    private void assertThatStreamIs(Publisher<byte[]> actualStream, String expected) {
        List<String> block = Flux.from(actualStream).map(String::new).collectList().block();
        String streamAsString = String.join("", block);
        assertThat(streamAsString).isEqualTo(expected);
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

        public TestScriptExecutionResult(Object content) {
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