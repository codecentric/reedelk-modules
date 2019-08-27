package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class PutStrategy extends AbstractExecutionStrategy {

    private final HttpClient.RequestSender sender;

    PutStrategy(HttpClient client, String baseUrl, boolean followRedirects) {
        super(baseUrl, followRedirects);
        this.sender = client.put();
    }

    @Override
    public <T> Mono<T> execute(String uri, BodyProvider bodyProvider, ResponseHandler<T> handler) {
        HttpClient.ResponseReceiver<?> sender = this.sender.send(bodyProvider.provide());
        return _request(sender, handler, uri);
    }
}
