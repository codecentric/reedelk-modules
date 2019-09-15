package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyDataProvider;
import com.reedelk.rest.client.BodyProvider;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_LENGTH;

public class PutStrategy extends AbstractExecutionStrategy {

    private final HttpClient.RequestSender sender;

    PutStrategy(HttpClient client, String baseUrl, boolean followRedirects) {
        super(baseUrl, followRedirects);
        this.sender = client.put();
    }

    @Override
    public <T> Mono<T> execute(String uri, BodyProvider bodyProvider, ResponseHandler<T> handler) {
        HttpClient.ResponseReceiver<?> receiver = sender.send((request, nettyOutbound) -> {
            BodyDataProvider data = bodyProvider.data(request);
            data.length().ifPresent(contentLength -> request.addHeader(CONTENT_LENGTH, String.valueOf(contentLength)));
            return nettyOutbound.send(data.get());
        });
        return _request(receiver, handler, uri);
    }
}
