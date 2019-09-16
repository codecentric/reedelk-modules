package com.reedelk.rest.client.strategy;

import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;

import java.util.function.BiFunction;

public interface ResponseHandler<ResponseType> extends BiFunction<HttpClientResponse, ByteBufMono, Mono<ResponseType>> {
}
