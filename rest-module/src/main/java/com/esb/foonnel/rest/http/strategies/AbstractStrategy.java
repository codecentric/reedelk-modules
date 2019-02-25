package com.esb.foonnel.rest.http.strategies;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.rest.commons.HeadersUtils;
import com.esb.foonnel.rest.route.Route;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

public abstract class AbstractStrategy implements RequestStrategy {

    @Override
    public Message handle(FullHttpRequest request, Route matchingRoute) throws Exception {
        Message inMessage = new Message();

        // Base Message properties
        inMessage.setRequestPath(request.uri());
        inMessage.setRequestMethod(request.method().name());
        inMessage.setRequestHttpHeaders(HeadersUtils.toMap(request.headers()));

        // Query Params
        QueryStringDecoder decoder = new QueryStringDecoder(inMessage.getRequestPath());
        Map<String, List<String>> requestQueryParams = decoder.parameters();
        inMessage.setRequestQueryParams(requestQueryParams);

        // Path Params
        Map<String, String> pathParams = matchingRoute.bindPathParams(inMessage.getRequestPath());
        inMessage.setRequestPathParams(pathParams);

        return handle0(inMessage, request);
    }

    protected Message handle0(Message inMessage, FullHttpRequest request) {
        return inMessage;
    }

}
