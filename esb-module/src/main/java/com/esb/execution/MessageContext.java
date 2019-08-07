package com.esb.execution;

import com.esb.api.component.OnResult;
import com.esb.api.message.Message;
import com.esb.commons.Preconditions;
import com.esb.internal.commons.SerializationUtils;

class MessageContext {

    private final OnResult callback;
    private Message message;

    MessageContext(Message message, OnResult callback) {
        this.message = message;
        this.callback = callback;
    }

    Message getMessage() {
        return message;
    }

    void replace(Message message) {
        Preconditions.checkState(message != null, "Message must not be null");
        this.message = message;
    }

    void onDone() {
        callback.onResult(message);
    }

    void onError(Throwable throwable) {
        callback.onError(throwable);
    }

    MessageContext copy() {
        Message messageClone = SerializationUtils.clone(message);
        return new MessageContext(messageClone, callback);
    }
}
