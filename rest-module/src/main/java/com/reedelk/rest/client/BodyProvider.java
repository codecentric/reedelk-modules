package com.reedelk.rest.client;

import org.reactivestreams.Publisher;

public interface BodyProvider {
    Publisher<byte[]> body();
}
