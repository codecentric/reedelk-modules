package com.reedelk.esb.execution;

import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.commons.SerializationUtils;

import static com.reedelk.esb.commons.Preconditions.checkState;

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

    EventContext copy() {
        Message messageClone = SerializationUtils.clone(message);
        return new EventContext(messageClone, callback);
    }
}
