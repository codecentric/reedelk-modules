package com.esb.execution;

import com.esb.api.component.OnResult;
import com.esb.api.message.Message;
import com.esb.internal.commons.SerializationUtils;

import static com.esb.commons.Preconditions.checkState;

class EventContext {

    private final OnResult callback;
    private Message message;

    EventContext(Message message, OnResult callback) {
        checkState(message != null, "message");
        checkState(callback != null, "callback");
        this.message = message;
        this.callback = callback;
    }

    Message getMessage() {
        return message;
    }

    void replaceWith(Message message) {
        checkState(message != null, "message");
        this.message = message;
    }

    void onDone() {
        callback.onResult(message);
    }

    void onError(Throwable throwable) {
        callback.onError(throwable);
    }

    EventContext copy() {
        Message messageClone = SerializationUtils.clone(message);
        return new EventContext(messageClone, callback);
    }
}
