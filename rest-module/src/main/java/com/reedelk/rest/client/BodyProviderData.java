package com.reedelk.rest.client;

import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;

public interface BodyProviderData {
    Publisher<? extends ByteBuf> provide();
    int length();
}
