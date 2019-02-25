package com.esb.foonnel.rest.http.strategies;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.rest.route.Route;
import io.netty.handler.codec.http.FullHttpRequest;

public class DELETERequestStrategy extends AbstractStrategy {
    @Override
    public Message handle(FullHttpRequest request, Route matchingRoute) throws Exception {
        return null;
    }
}
