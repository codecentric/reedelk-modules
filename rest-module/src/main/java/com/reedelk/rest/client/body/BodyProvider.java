package com.reedelk.rest.client.body;

import org.reactivestreams.Publisher;

public interface BodyProvider {
    /**
     * Used by normal transfer encoding. Length
     * of the payload is known in advance.
     * @return the byte array to be sent to the remote host.
     */
    default byte[] asByteArray() {
        throw new UnsupportedOperationException();
    }

    /**
     * Used by chunked transfer encoding.
     * @return the byte array stream to be sent to the remote host.
     */
    default Publisher<byte[]> asStream() {
        throw new UnsupportedOperationException();
    }
}
