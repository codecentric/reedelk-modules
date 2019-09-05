package com.reedelk.esb.execution;

import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.commons.SerializationUtils;

import static com.reedelk.esb.commons.Preconditions.checkState;

class MessageAndContext {

    private final Context context;
    private Message message;

    MessageAndContext(Message message, Context context) {
        checkState(message != null, "message");
        this.message = message;
        this.context = context;
    }

    Message getMessage() {
        return message;
    }

    public Context getContext() {
        return context;
    }

    void replaceWith(Message message) {
        checkState(message != null, "message");
        this.message = message;
    }

    MessageAndContext copy() {
        Message messageClone = SerializationUtils.clone(message);
        return new MessageAndContext(messageClone, context);
    }
}
