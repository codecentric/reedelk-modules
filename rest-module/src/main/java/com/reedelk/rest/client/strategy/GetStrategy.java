package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

class GetStrategy extends AbstractExecutionStrategy {

    private HttpClient.ResponseReceiver<?> receiver;

    GetStrategy(HttpClient client, String baseUrl, boolean followRedirects) {
        super(baseUrl, followRedirects);
        receiver = client.get();
    }

    @Override
    public <T> Mono<T> execute(String uri, BodyProvider bodyProvider, ResponseHandler<T> handler) {
        return _request(receiver, handler, uri);
    }
}
