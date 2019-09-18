package com.reedelk.rest.apacheclient;

import reactor.core.publisher.Flux;

public interface BodyProvider {
    Flux<byte[]> body();
}
