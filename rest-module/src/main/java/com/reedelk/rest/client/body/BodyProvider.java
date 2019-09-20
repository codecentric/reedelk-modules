package com.reedelk.rest.client.body;

import org.reactivestreams.Publisher;

public interface BodyProvider {

    Publisher<byte[]> body();

}
