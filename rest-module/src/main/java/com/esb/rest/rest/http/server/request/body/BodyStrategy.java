package com.esb.rest.rest.http.server.request.body;

import io.netty.handler.codec.http.FullHttpRequest;

public interface BodyStrategy<T> {

    BodyStrategyResult<T> execute(FullHttpRequest request) throws Exception;

}
