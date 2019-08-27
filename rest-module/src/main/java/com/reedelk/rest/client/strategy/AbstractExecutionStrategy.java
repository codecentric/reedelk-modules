package com.reedelk.rest.client.strategy;

import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient.ResponseReceiver;
import reactor.netty.http.client.HttpClientResponse;

abstract class AbstractExecutionStrategy implements ExecutionStrategy {

    private String baseUrl;
    private boolean followRedirects;

    AbstractExecutionStrategy(String baseUrl, boolean followRedirects) {
        this.followRedirects = followRedirects;
        this.baseUrl = baseUrl;
    }

    <T> Mono<T> _request(ResponseReceiver<?> receiver, ResponseHandler<T> handler, String uri) {

        return receiver.uri(uri).responseSingle((response, byteBufMono) ->
                isRedirect(response) ?
                        redirect(receiver, handler, response) : // redirect
                        handler.apply(response, byteBufMono)); // normal flow
    }

    private <T> Mono<T> redirect(ResponseReceiver<?> receiver, ResponseHandler<T> handler, HttpClientResponse response) {

        String redirectUrl = getLocationHeader(response);
        if (redirectUrl.startsWith("http")) {
            // Location is Absolute (e.g http://mydomain/redirect/url
            return _request(receiver, handler, redirectUrl);
        } else {
            // Location is relative (.e.g /redirect/path
            return _request(receiver, handler, baseUrl + redirectUrl);
        }
    }

    private boolean isRedirect(HttpClientResponse response) {
        return followRedirects &&
                (response.status() == HttpResponseStatus.MOVED_PERMANENTLY ||
                        response.status() == HttpResponseStatus.FOUND ||
                        response.status() == HttpResponseStatus.SEE_OTHER);
    }

    private String getLocationHeader(HttpClientResponse response) {
        return response.responseHeaders().get("Location");
    }
}
