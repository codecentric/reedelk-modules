package com.esb.foonnel.rest.mapper;

import com.esb.foonnel.api.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class MessageToHttpResponse implements Mapper<Message, FullHttpResponse> {

    @Override
    public FullHttpResponse map(Message message) {

        int httpStatus = message.getHttpStatus();
        boolean hasContentType = message.getResponseHttpHeaders().keySet().contains(CONTENT_TYPE.toString());

        CharSequence contentType = hasContentType ? message.getResponseHttpHeaders().get(CONTENT_TYPE.toString()) : TEXT_PLAIN;

        ByteBuf entity = Unpooled.wrappedBuffer(message.getContent().getBytes(StandardCharsets.UTF_8));
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                HttpResponseStatus.valueOf(httpStatus),
                entity);

        response.headers().add(CONTENT_TYPE, contentType);
        return response;
    }
}
