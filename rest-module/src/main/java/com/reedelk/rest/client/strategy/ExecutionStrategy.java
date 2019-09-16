package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import reactor.core.publisher.Mono;

public interface ExecutionStrategy {

    <T> Mono<T> execute(String uri,
                        BodyProvider bodyProvider,
                        ResponseHandler<T> handler);
}
