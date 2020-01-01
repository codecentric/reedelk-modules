package com.reedelk.rabbitmq.commons;

import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.reedelk.rabbitmq.component.RabbitMQConsumer;
import com.reedelk.runtime.api.commons.StreamUtils;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.DefaultMessageAttributes;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.reedelk.runtime.api.commons.Preconditions.checkArgument;

public class ConsumerDeliverCallback implements DeliverCallback {

    private final InboundEventListener listener;
    private final MimeType consumedMessageMimeType;

    public ConsumerDeliverCallback(InboundEventListener listener, MimeType consumedMessageMimeType) {
        checkArgument(listener != null, "listener");
        checkArgument(consumedMessageMimeType != null, "consumedMessageMimeType");
        this.listener = listener;
        this.consumedMessageMimeType = consumedMessageMimeType;
    }

    @Override
    public void handle(String consumerTag, Delivery delivery) throws IOException {
        // Message Attributes
        Map<String, Object> headers = delivery.getProperties().getHeaders();

        byte[] content = delivery.getBody();
        TypedContent<?> typedContent = StreamUtils.FromByteArray.asTypedContent(content, consumedMessageMimeType);

        Map<String, Serializable> attributes = new HashMap<>();
        MessageAttributes messageAttributes = new DefaultMessageAttributes(RabbitMQConsumer.class, attributes);

        Message inboundMessage = MessageBuilder.get()
                .typedContent(typedContent)
                .mimeType(consumedMessageMimeType)
                .attributes(messageAttributes)
                .build();

        listener.onEvent(inboundMessage, new OnResult() {});
    }
}