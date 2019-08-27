package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class HeadStrategy extends AbstractExecutionStrategy {

    private final HttpClient.ResponseReceiver<?> receiver;

    HeadStrategy(HttpClient client, String baseUrl, boolean followRedirects) {
        super(baseUrl, followRedirects);
        this.receiver = client.head();
    }

    @Override
    public <T> Mono<T> execute(String uri, BodyProvider bodyProvider, ResponseHandler<T> handler) {
        return _request(receiver, handler, uri);
    }
}
