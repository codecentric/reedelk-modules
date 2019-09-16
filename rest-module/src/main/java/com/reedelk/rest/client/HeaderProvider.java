package com.reedelk.rest.client;

import io.netty.handler.codec.http.HttpHeaders;

public interface HeaderProvider {

    void provide(HttpHeaders headers);
}
