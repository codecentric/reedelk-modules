package com.esb.foonnel.rest.http.server;

import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.rest.commons.OutboundProperty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.valueOf;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpResponseMapper {

    public FullHttpResponse map(HttpVersion httpVersion, Message message) {

        byte[] bytes = new byte[0];
        if (message.getContent().getType().getTypeClass().isAssignableFrom(byte[].class)) {
            bytes = (byte[]) message.getContent().getContent();
        }
        if (message.getContent().getType().getTypeClass().isAssignableFrom(String.class)) {
            bytes = ((String) message.getContent().getContent()).getBytes();
        }

        ByteBuf entity = Unpooled.wrappedBuffer(bytes);

        Map<String, String> outboundHeaders = OutboundProperty.HEADERS.getMap(message);
        boolean hasContentType = outboundHeaders.containsKey(CONTENT_TYPE.toString());
        CharSequence contentType = hasContentType ? outboundHeaders.get(CONTENT_TYPE.toString()) : TEXT_PLAIN;

        HttpResponseStatus httpStatus = valueOf(OutboundProperty.STATUS.getInt(message));
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpVersion, httpStatus, entity);

        HttpHeaders headers = response.headers();
        headers.add(CONTENT_TYPE, contentType);
        headers.add(CONTENT_LENGTH, bytes.length);

        for (Map.Entry<String,String> header : outboundHeaders.entrySet()) {
            headers.add(header.getKey(), header.getValue());
        }

        return response;
    }

    public FullHttpResponse fromHttpStatus(HttpVersion httpVersion, HttpResponseStatus status) {
        String content = status.reasonPhrase();
        byte[] bytes = content.getBytes(UTF_8);
        ByteBuf entity = Unpooled.wrappedBuffer(bytes);

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpVersion, status, entity);

        HttpHeaders headers = response.headers();
        headers.add(CONTENT_TYPE, TEXT_PLAIN);
        headers.add(CONTENT_LENGTH, bytes.length);
        return response;
    }
}
