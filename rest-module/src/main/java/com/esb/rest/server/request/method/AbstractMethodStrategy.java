package com.esb.rest.server.request.method;

import com.esb.api.message.Message;
import com.esb.rest.commons.HeadersUtils;
import com.esb.rest.commons.InboundProperty;
import com.esb.rest.server.route.Route;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;
import java.util.Map;

abstract class AbstractMethodStrategy implements MethodStrategy {

    @Override
    public Message execute(FullHttpRequest request, Route matchingRoute) throws Exception {
        Message inMessage = new Message();

        // Path, Method and Headers
        InboundProperty.PATH.set(inMessage, request.uri());
        InboundProperty.METHOD.set(inMessage, request.method().name());
        InboundProperty.HEADERS.set(inMessage, HeadersUtils.toMap(request.headers()));

        // Path Params
        Map<String, String> pathParams = matchingRoute.bindPathParams(request.uri());
        InboundProperty.PATH_PARAMS.set(inMessage, pathParams);

        // Query Params
        Map<String, List<String>> queryParams = matchingRoute.queryParameters(request);
        InboundProperty.QUERY_PARAMS.set(inMessage, queryParams);

        return inMessage;
    }

}
