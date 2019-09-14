package com.reedelk.rest.client;

import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;

import java.util.Optional;

public interface BodyDataProvider {

    /**
     * Provides a byte buffer publisher holding the data to be
     * sent in the request.
     */
    Publisher<? extends ByteBuf> get();

    /**
     * The length of a request body is optional because if the
     * body of the message is a stream based content, then the
     * length is not known, and the client uses chunked transfer
     * encoding. If the body of the message is NOT stream based,
     * then the content length header is set and chunked transfer
     * encoding IS NOT used.
     */
    Optional<Integer> length();
}
