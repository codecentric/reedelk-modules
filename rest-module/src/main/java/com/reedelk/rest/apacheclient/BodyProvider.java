package com.reedelk.rest.apacheclient;

import org.reactivestreams.Publisher;

public interface BodyProvider {
    Publisher<byte[]> body();
}
