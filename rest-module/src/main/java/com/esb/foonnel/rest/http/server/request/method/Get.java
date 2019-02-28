package com.esb.foonnel.rest.http.server.request.method;


import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.rest.http.server.request.body.BodyStrategyBuilder;
import com.esb.foonnel.rest.http.server.request.body.BodyStrategyResult;
import com.esb.foonnel.rest.http.server.route.Route;
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
