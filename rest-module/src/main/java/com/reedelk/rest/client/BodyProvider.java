package com.reedelk.rest.client;

import reactor.netty.http.client.HttpClientRequest;

public interface BodyProvider {

    BodyDataProvider data(HttpClientRequest request);
}
