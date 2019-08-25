package com.reedelk.rest.client;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import java.util.function.BiFunction;

public class Client {

    private String baseUrl;
    private boolean followRedirects = true;

    private HttpClient.ResponseReceiver<?> receiver;

    Client(HttpClient.ResponseReceiver<?> receiver) {
        this.receiver = receiver;
    }

    void baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    void followRedirects(Boolean followRedirects) {
        if (followRedirects != null) {
            this.followRedirects = followRedirects;
        }
    }

    public Flux<byte[]> execute(String uri, BiFunction<HttpClientResponse, ByteBufFlux, Publisher<byte[]>> handler) {
        return executeInternal(baseUrl + uri, handler);
    }

    private Flux<byte[]> executeInternal(String uri, BiFunction<HttpClientResponse, ByteBufFlux, Publisher<byte[]>> handler) {
        return receiver.uri(uri).response((response, byteBufFlux) -> {
            if (followRedirects) {
                return handleFollowRedirects(handler, response, byteBufFlux);
            } else {
                return handler.apply(response, byteBufFlux);
            }
        });
    }

    private Publisher<byte[]> handleFollowRedirects(BiFunction<HttpClientResponse, ByteBufFlux, Publisher<byte[]>> handler, HttpClientResponse response, ByteBufFlux byteBufFlux) {
        if (isRedirect(response)) {
            String redirectUrl = getLocationHeader(response);
            // Absolute
            if (redirectUrl.startsWith("http")) {
                return executeInternal(redirectUrl, handler);

                // Location is relative
            } else {
                return executeInternal(baseUrl + redirectUrl, handler);
            }
            // No redirect
        } else {
            return handler.apply(response, byteBufFlux);
        }
    }

    private String getLocationHeader(HttpClientResponse response) {
        return response.responseHeaders().get("Location");
    }

    private boolean isRedirect(HttpClientResponse response) {
        return response.status() == HttpResponseStatus.MOVED_PERMANENTLY ||
                response.status() == HttpResponseStatus.FOUND ||
                response.status() == HttpResponseStatus.SEE_OTHER;
    }

}
