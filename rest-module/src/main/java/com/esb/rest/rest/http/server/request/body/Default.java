package com.esb.rest.rest.http.server.request.body;

import com.esb.api.message.MemoryTypedContent;
import com.esb.api.message.MimeType;
import com.esb.api.message.Type;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class Default implements BodyStrategy {

    @Override
    public BodyStrategyResult execute(FullHttpRequest request) {
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

        MimeType payloadMimeType = MimeType.parse(contentType);
        if (payloadMimeType.equals(MimeType.APPLICATION_JSON)) {
            // Payload is string
            Type type = new Type(payloadMimeType, String.class);
            // The charset should be fixed.
            MemoryTypedContent<String> memoryTypedContent = new MemoryTypedContent<>(new String(bytes, StandardCharsets.UTF_8), type);
            return new BodyStrategyResult<>(memoryTypedContent, Collections.emptyList());


        } else {
            Type type = new Type(payloadMimeType, byte[].class);
            MemoryTypedContent<byte[]> memoryTypedContent = new MemoryTypedContent<>(bytes, type);
            return new BodyStrategyResult<>(memoryTypedContent, Collections.emptyList());
        }
    }

}
