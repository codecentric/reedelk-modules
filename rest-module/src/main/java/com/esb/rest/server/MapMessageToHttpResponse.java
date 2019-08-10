package com.esb.rest.server;

import com.esb.api.message.Message;
import com.esb.rest.commons.OutboundProperty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.valueOf;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MapMessageToHttpResponse {

    private final HttpVersion httpVersion;

    MapMessageToHttpResponse(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    // TODO: This method is crap.
    FullHttpResponse map(Message message) {

        // Need to convert

        byte[] bytes = new byte[0];
        Object content = message.getTypedContent().getContent();
        if (content instanceof String) {
            bytes = ((String) content).getBytes();
        } else if (content != null) {
            bytes = content.toString().getBytes();
        }

        ByteBuf entity = Unpooled.wrappedBuffer(bytes);

        String contentType = TEXT_PLAIN.toString();
        Map<String, String> outboundHeaders = new HashMap<>();

        if (OutboundProperty.HEADERS.isDefined(message)) {
            outboundHeaders = OutboundProperty.HEADERS.getMap(message);
            if (outboundHeaders.containsKey(CONTENT_TYPE.toString())) {
                contentType = outboundHeaders.get(CONTENT_TYPE.toString());
            }
        }

        HttpResponseStatus httpStatus = valueOf(OutboundProperty.STATUS.getInt(message));

        return buildResponse(entity, httpStatus, contentType, bytes.length, outboundHeaders);
    }

    FullHttpResponse fromStatus(HttpResponseStatus status) {
        String content = status.reasonPhrase();
        byte[] bytes = content.getBytes(UTF_8);
        ByteBuf entity = Unpooled.wrappedBuffer(bytes);
        return buildResponse(entity, status, TEXT_PLAIN.toString(), bytes.length, Collections.emptyMap());
    }

    private FullHttpResponse buildResponse(ByteBuf entity, HttpResponseStatus status, String contentType, int length, Map<String, String> additionalHeaders) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpVersion, status, entity);
        HttpHeaders headers = response.headers();
        headers.add(CONTENT_TYPE, contentType);
        headers.add(CONTENT_LENGTH, length);

        for (Map.Entry<String,String> header : additionalHeaders.entrySet()) {
            getMatchingHeader(headers, header.getKey()).ifPresent(headers::remove);
            headers.set(header.getKey(), header.getValue());
        }
        return response;
    }

    private Optional<String> getMatchingHeader(HttpHeaders headers, String targetHeaderName) {
        for (String headerName : headers.names()) {
            if (headerName.toLowerCase().equals(targetHeaderName.toLowerCase())) {
                return Optional.of(headerName);
            }
        }
        return Optional.empty();
    }
}
