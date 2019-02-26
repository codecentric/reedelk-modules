package com.esb.foonnel.rest.http.strategies;

import com.esb.foonnel.api.*;
import com.esb.foonnel.rest.http.InboundProperty;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;

public class GETRequestStrategy extends AbstractStrategy {

    @Override
    protected Message handle0(Message inMessage, FullHttpRequest request) {

        ByteBuf buf = request.content();
        byte[] bytes;

        int length = buf.readableBytes();

        if (buf.hasArray()) {
            bytes = buf.array();
        } else {
            bytes = new byte[length];
            buf.getBytes(buf.readerIndex(), bytes);
        }

        String contentType = InboundProperty.Headers.CONTENT_TYPE.get(inMessage);

        ContentType type = new ContentType(MimeType.parse(contentType), byte[].class);
        TypedContent<byte[]> content = new MemoryTypedContent<>(bytes, type);
        inMessage.setContent(content);
        return inMessage;
    }
}
