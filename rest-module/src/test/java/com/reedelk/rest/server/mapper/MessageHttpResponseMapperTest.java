package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerResponse;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MessageHttpResponseMapperTest {

    @Mock
    private FlowContext context;
    @Mock
    private HttpServerResponse response;
    @Mock
    private ScriptEngineService scriptEngine;

    @Test
    void shouldDoSomething() {
        // Given
        String responseBody = null;
        String responseStatus = null;

        MessageHttpResponseMapper mapper = newMapper(responseBody, responseStatus);

        Message message = MessageBuilder.get().text("hello").build();

        // When
        Publisher<byte[]> outDataStream = mapper.map(message, response, context);

        // Then
        assertThat(outDataStream).isNotNull();
    }

    private MessageHttpResponseMapper newMapper(String responseBody, String responseStatus) {
        return new MessageHttpResponseMapper(
                scriptEngine, responseBody, responseStatus,
                new HashMap<>(),null,null);
    }

}