package com.reedelk.rabbitmq.commons;

import com.rabbitmq.client.Delivery;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.MimeType;

import static com.reedelk.runtime.api.commons.Preconditions.checkArgument;

public class ConsumerDeliverCallbackAutoAck extends ConsumerDeliverCallback {

    private final InboundEventListener listener;

    public ConsumerDeliverCallbackAutoAck(InboundEventListener listener, MimeType consumedMessageMimeType) {
        super(consumedMessageMimeType);
        checkArgument(listener != null, "listener");
        this.listener = listener;
    }

    @Override
    protected void onEvent(Message message, Delivery delivery) {
        // Notify Event
        listener.onEvent(message, new OnResult() {});
    }
}
