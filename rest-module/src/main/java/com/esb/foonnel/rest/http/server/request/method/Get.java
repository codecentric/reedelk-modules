package com.esb.foonnel.rest.http.server.request.method;


import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.api.message.TypedContent;
import io.netty.handler.codec.http.FullHttpRequest;

public class Get extends AbstractMethodStrategy {

    @Override
    protected Message handle0(Message inMessage, FullHttpRequest request) {
        TypedContent<byte[]> content = extractBodyContent(inMessage, request);
        inMessage.setContent(content);
        return inMessage;
    }

}
