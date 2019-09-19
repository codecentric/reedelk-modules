package com.reedelk.rest.client;

import com.reedelk.runtime.api.commons.ConsumeByteArrayStream;
import com.reedelk.runtime.api.exception.ESBException;
import org.reactivestreams.Publisher;

public class ErrorResponse extends ESBException {

    private final Publisher<byte[]> data;

    public ErrorResponse(Publisher<byte[]> data) {
        super();
        this.data = data;
    }

    @Override
    public String getMessage() {
        byte[] from = ConsumeByteArrayStream.from(data);
        return new String(from);
    }
}
