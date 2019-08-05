package com.esb.executor;

import com.esb.api.component.ResultCallback;
import com.esb.api.message.Message;
import com.esb.commons.Preconditions;

public class ReactiveMessageContext {

    private final ResultCallback callback;
    private Message message;

    public ReactiveMessageContext(Message message, ResultCallback callback) {
        this.message = message;
        this.callback = callback;
    }

    public Message getMessage() {
        return message;
    }

    public void replace(Message message) {
        Preconditions.checkState(message != null, "Message must not be null");
        this.message = message;
    }

    public void onDone() {
        callback.onResult(message);
    }

    public void onError(Throwable throwable) {
        callback.onError(throwable);
    }
}
