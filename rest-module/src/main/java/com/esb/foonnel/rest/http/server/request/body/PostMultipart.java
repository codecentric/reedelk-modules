package com.esb.foonnel.rest.http.server.request.body;

import com.esb.foonnel.api.message.Message;
import io.netty.handler.codec.http.FullHttpRequest;

public class PostMultipart implements BodyStrategy {

    @Override
    public BodyStrategyResult execute(FullHttpRequest request) throws Exception {
        return null;
    }
}
