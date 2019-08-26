package com.reedelk.rest.client;

import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;

public interface BodyProvider {

    Publisher<? extends ByteBuf> provide();
}
