package com.esb.foonnel.rest.http.server.request.body;

import io.netty.handler.codec.http.FullHttpRequest;

public interface BodyStrategy<T> {

    BodyStrategyResult<T> execute(FullHttpRequest request) throws Exception;

}
