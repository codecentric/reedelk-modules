package com.reedelk.rest.client;

import com.reedelk.runtime.api.commons.ConsumeByteArrayStream;
import com.reedelk.runtime.api.exception.ESBException;
import org.reactivestreams.Publisher;

public class ErrorResponseException extends ESBException {

    private Publisher<byte[]> data;
    private String message;

    public ErrorResponseException(Publisher<byte[]> data) {
        super();
        this.data = data;
    }

    // The method get message from an exception might be called
    // multiple times, however, the stream can only be consumed once.
    // That is why we keep a reference of the read message from the stream
    // in order to use it multiple times if needed.
    @Override
    public String getMessage() {
        if (message == null) {
            synchronized (this) {
                if (message == null) {
                    byte[] from = ConsumeByteArrayStream.from(data);
                    message = new String(from);
                    data = null;
                }
            }
        }
        return message;
    }
}
