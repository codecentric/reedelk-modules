package com.reedelk.rest.client.body;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.reactivestreams.Publisher;

public interface BodyProvider {
    /**
     * Used by normal transfer encoding. Length
     * of the payload is known in advance.
     * @return the byte array to be sent to the remote host.
     */
    default byte[] asByteArray(Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    /**
     * Used by chunked transfer encoding.
     * @return the byte array stream to be sent to the remote host.
     */
    default Publisher<byte[]> asStream(Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks whether the content is streamable or not.
     * @return true if the content is a stream based content, false otherwise.
     * @param message the input message.
     */
    default boolean streamable(Message message) {
        return false;
    }
}
