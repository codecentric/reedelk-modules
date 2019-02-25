package com.esb.foonnel.rest.http.strategies;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.rest.route.Route;
import io.netty.handler.codec.http.FullHttpRequest;

public interface RequestStrategy {

    Message handle(FullHttpRequest request, Route matchingRoute) throws Exception;

}
