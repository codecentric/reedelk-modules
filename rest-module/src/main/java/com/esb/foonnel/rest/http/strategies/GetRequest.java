package com.esb.foonnel.rest.http.strategies;


import com.esb.foonnel.api.message.*;
import com.esb.foonnel.rest.http.InboundProperty;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;

public class GetRequest extends AbstractStrategy {

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

        // TODO: The type can be inferred from the content.
        Type type = new Type(MimeType.parse(contentType), byte[].class);
        TypedContent<byte[]> content = new MemoryTypedContent<>(bytes, type);

        inMessage.setContent(content);
        return inMessage;
    }
}
