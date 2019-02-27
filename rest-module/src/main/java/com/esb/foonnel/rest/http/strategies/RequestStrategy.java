package com.esb.foonnel.rest.http.strategies;

import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.rest.route.Route;
import io.netty.handler.codec.http.FullHttpRequest;

public interface RequestStrategy {

    Message execute(FullHttpRequest request, Route matchingRoute) throws Exception;

}
