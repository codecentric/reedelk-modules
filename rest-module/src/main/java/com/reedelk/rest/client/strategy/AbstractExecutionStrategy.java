package com.reedelk.rest.client.strategy;

import com.reedelk.rest.commons.HttpHeader;
import com.reedelk.rest.commons.IsRedirection;
import com.reedelk.runtime.api.commons.StringUtils;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient.ResponseReceiver;
import reactor.netty.http.client.HttpClientResponse;

abstract class AbstractExecutionStrategy implements ExecutionStrategy {

    private static final String PROTOCOL_HTTP = "http";

    private String baseUrl;
    private boolean followRedirects;

    AbstractExecutionStrategy(String baseUrl, boolean followRedirects) {
        this.followRedirects = followRedirects;
        this.baseUrl = baseUrl;
    }

    <T> Mono<T> _request(ResponseReceiver<?> receiver, ResponseHandler<T> handler, String uri) {

        return receiver.uri(baseUrl + uri).responseSingle((response, byteBufMono) ->
                shouldRedirect(response) ?
                        redirect(receiver, handler, response) : // redirect
                        handler.apply(response, byteBufMono)); // normal flow
    }

    private <T> Mono<T> redirect(ResponseReceiver<?> receiver, ResponseHandler<T> handler, HttpClientResponse response) {
        String redirectUrl = getLocationHeader(response);
        // Location is Absolute (e.g http://mydomain/redirect/url
        if (redirectUrl.toLowerCase().startsWith(PROTOCOL_HTTP)) {
            return _request(receiver, handler, redirectUrl);
        } else {
            // Location is relative (.e.g /redirect/path
            return _request(receiver, handler, baseUrl + redirectUrl);
        }
    }

    private boolean shouldRedirect(HttpClientResponse response) {
        return followRedirects && IsRedirection.status(response.status());
    }

    private String getLocationHeader(HttpClientResponse response) {
        if (response.responseHeaders().contains(HttpHeader.LOCATION)) {
            return response.responseHeaders().get(HttpHeader.LOCATION);
        } else {
            return StringUtils.EMPTY;
        }
    }
}
