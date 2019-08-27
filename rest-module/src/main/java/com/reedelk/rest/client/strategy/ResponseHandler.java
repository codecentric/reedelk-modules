package com.reedelk.rest.client.strategy;

import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;

import java.util.function.BiFunction;

public interface ResponseHandler<T> extends BiFunction<HttpClientResponse, ByteBufMono, Mono<T>> {
}
