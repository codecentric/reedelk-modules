package com.esb.foonnel.rest.http.server.request.body;

import com.esb.foonnel.api.message.MemoryTypedContent;
import com.esb.foonnel.api.message.MimeType;
import com.esb.foonnel.api.message.Type;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.Collections;

public class Default implements BodyStrategy<byte[]> {

    @Override
    public BodyStrategyResult<byte[]> execute(FullHttpRequest request) {
        ByteBuf buf = request.content();
        byte[] bytes;

        int length = buf.readableBytes();

        if (buf.hasArray()) {
            bytes = buf.array();
        } else {
            bytes = new byte[length];
            buf.getBytes(buf.readerIndex(), bytes);
        }

        String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);

        Type type = new Type(MimeType.parse(contentType), byte[].class);
        MemoryTypedContent<byte[]> memoryTypedContent = new MemoryTypedContent<>(bytes, type);

        return new BodyStrategyResult<>(memoryTypedContent, Collections.emptyList());
    }

}
