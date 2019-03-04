package com.esb.rest.rest.http.server.request.method;

import com.esb.api.message.Message;
import com.esb.rest.rest.http.server.request.body.BodyStrategyBuilder;
import com.esb.rest.rest.http.server.request.body.BodyStrategyResult;
import com.esb.rest.rest.http.server.route.Route;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;

class Post extends AbstractMethodStrategy {

    @Override
    public Message execute(FullHttpRequest request, Route matchingRoute) throws Exception {
        Message message = super.execute(request, matchingRoute);

        BodyStrategyResult<Map> compoundBodyContent = BodyStrategyBuilder
                .from(Map.class, request)
                .execute(request);

        message.setTypedContent(compoundBodyContent.getContent());
        message.setParts(compoundBodyContent.getParts());

        return message;
    }
}
