package com.esb.rest.server.request.method;


import com.esb.api.message.Message;
import com.esb.rest.server.request.body.BodyStrategyBuilder;
import com.esb.rest.server.request.body.BodyStrategyResult;
import com.esb.rest.server.route.Route;
import io.netty.handler.codec.http.FullHttpRequest;

class Get<T> extends AbstractMethodStrategy {

    @Override
    public Message execute(FullHttpRequest request, Route matchingRoute) throws Exception {
        Message message = super.execute(request, matchingRoute);

        BodyStrategyResult<byte[]> compoundBodyContent = BodyStrategyBuilder
                .from(byte[].class, request)
                .execute(request);

        message.setTypedContent(compoundBodyContent.getContent());
        return message;
    }

}
