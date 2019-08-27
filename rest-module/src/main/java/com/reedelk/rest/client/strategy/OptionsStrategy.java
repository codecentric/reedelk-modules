package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class OptionsStrategy extends AbstractExecutionStrategy {

    private final HttpClient.ResponseReceiver<?> receiver;

    OptionsStrategy(HttpClient client, String baseUrl, boolean followRedirects) {
        super(baseUrl, followRedirects);
        this.receiver = client.options();
    }

    @Override
    public <T> Mono<T> execute(String uri, BodyProvider bodyProvider, ResponseHandler<T> handler) {
        return _request(receiver, handler, uri);
    }
}
