package com.reedelk.rest.client;

import com.reedelk.runtime.api.commons.ConsumeByteArrayStream;
import com.reedelk.runtime.api.exception.ESBException;
import org.apache.http.HttpResponse;
import org.reactivestreams.Publisher;

public class HttpClientResponseException extends ESBException {

    private final HttpResponse response;
    private Publisher<byte[]> data;
    private String message;

    public HttpClientResponseException(HttpResponse response, Publisher<byte[]> data) {
        super();
        this.data = data;
        this.response = response;
    }

    // The method get message from an exception might be called
    // multiple times, however, the stream can only be consumed once.
    // That is why we keep a reference of the read message from the stream
    // in order to use it multiple times if needed.
    @Override
    public synchronized String getMessage() {
        if (message == null) {
            byte[] from = ConsumeByteArrayStream.from(data);
            message = new String(from);
            data = null;
        }
        return message;
    }

    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    public String getReasonPhrase() {
        return response.getStatusLine().getReasonPhrase();
    }
}
