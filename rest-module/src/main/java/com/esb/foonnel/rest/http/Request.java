package com.esb.foonnel.rest.http;

import io.netty.handler.codec.http.FullHttpRequest;

import java.nio.charset.StandardCharsets;

public class Request {

    private final FullHttpRequest request;

    public Request(final FullHttpRequest request) {
        this.request = request;
    }

    public String body() {
        return request.content().toString(StandardCharsets.UTF_8);
    }
}
