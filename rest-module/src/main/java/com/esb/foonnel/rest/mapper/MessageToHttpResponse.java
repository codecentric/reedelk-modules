package com.esb.foonnel.rest.mapper;

import com.esb.foonnel.api.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.valueOf;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class MessageToHttpResponse implements Mapper<Message, FullHttpResponse> {

    @Override
    public FullHttpResponse map(Message message) {

        int httpStatus = message.getHttpStatus();
        byte[] bytes = message.getContent().getBytes(StandardCharsets.UTF_8);


        ByteBuf entity = Unpooled.wrappedBuffer(bytes);

        boolean hasContentType = message.getResponseHttpHeaders().keySet().contains(CONTENT_TYPE.toString());
        CharSequence contentType = hasContentType ? message.getResponseHttpHeaders().get(CONTENT_TYPE.toString()) : TEXT_PLAIN;


        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, valueOf(httpStatus), entity);

        HttpHeaders headers = response.headers();
        headers.add(CONTENT_TYPE, contentType);
        headers.add(CONTENT_LENGTH, bytes.length);
        return response;
    }
}
