package com.esb.rest.server.request.method;

import com.esb.api.message.Message;
import com.esb.rest.server.route.Route;
import io.netty.handler.codec.http.FullHttpRequest;

public interface MethodStrategy {

    Message execute(FullHttpRequest request, Route matchingRoute) throws Exception;

}
