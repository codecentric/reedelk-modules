package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class DeleteStrategy extends AbstractExecutionStrategy {

    private final HttpClient.RequestSender sender;

    DeleteStrategy(HttpClient client, String baseUrl, boolean followRedirects) {
        super(baseUrl, followRedirects);
        this.sender = client.delete();
    }

    @Override
    public <T> Mono<T> execute(String uri, BodyProvider bodyProvider, ResponseHandler<T> handler) {
        HttpClient.ResponseReceiver<?> receiver = sender.send(bodyProvider.provide());
        return _request(receiver, handler, uri);
    }
}
