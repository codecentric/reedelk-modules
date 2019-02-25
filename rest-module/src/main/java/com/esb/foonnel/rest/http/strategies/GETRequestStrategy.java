package com.esb.foonnel.rest.http.strategies;

import com.esb.foonnel.api.Message;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;

public class GETRequestStrategy extends AbstractStrategy {

    @Override
    protected Message handle0(Message inMessage, FullHttpRequest request) {

        ByteBuf buf = request.content();
        byte[] bytes;
        int offset;
        int length = buf.readableBytes();

        if (buf.hasArray()) {
            bytes = buf.array();
            offset = buf.arrayOffset();
        } else {
            bytes = new byte[length];
            buf.getBytes(buf.readerIndex(), bytes);
            offset = 0;
        }


        inMessage.setContent(bytes);
        return inMessage;
    }
}
