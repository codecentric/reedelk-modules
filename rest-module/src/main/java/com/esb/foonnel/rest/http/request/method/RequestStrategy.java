package com.esb.foonnel.rest.http.request.method;

import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.rest.http.route.Route;
import io.netty.handler.codec.http.FullHttpRequest;

public interface RequestStrategy {

    Message execute(FullHttpRequest request, Route matchingRoute) throws Exception;

}
