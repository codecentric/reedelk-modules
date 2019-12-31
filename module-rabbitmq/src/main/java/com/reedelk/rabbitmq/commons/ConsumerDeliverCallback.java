package com.reedelk.rabbitmq.commons;

import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;

import java.io.IOException;
import java.util.Map;

import static com.reedelk.runtime.api.commons.Preconditions.checkArgument;

public class ConsumerDeliverCallback implements DeliverCallback {

    private final InboundEventListener listener;

    public ConsumerDeliverCallback(InboundEventListener listener) {
        checkArgument(listener != null, "listener");
        this.listener = listener;
    }

    @Override
    public void handle(String consumerTag, Delivery delivery) throws IOException {
        // Message Attributes
        Map<String, Object> headers = delivery.getProperties().getHeaders();


        // This one depends  on the mime type
        String message = new String(delivery.getBody(), "UTF-8");

        Message inboundMessage = MessageBuilder.get()
                .withText(message)
                .build();

        // TODO: Create calback without on result. Should be empty
        listener.onEvent(inboundMessage, new OnResult() {});
    }
}