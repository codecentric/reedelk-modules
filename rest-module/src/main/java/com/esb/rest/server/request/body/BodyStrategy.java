package com.esb.rest.server.request.body;

import io.netty.handler.codec.http.FullHttpRequest;

public interface BodyStrategy<T> {

    BodyStrategyResult<T> execute(FullHttpRequest request) throws Exception;

}
