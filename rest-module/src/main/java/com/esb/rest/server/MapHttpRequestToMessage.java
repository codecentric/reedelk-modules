package com.esb.rest.server;

import com.esb.api.message.*;
import com.esb.rest.commons.HttpHeadersAsMap;
import com.esb.rest.commons.InboundProperty;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.QueryStringDecoder;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;

import java.util.List;
import java.util.Map;

public class MapHttpRequestToMessage {

    public static Message from(HttpServerRequest request) {
        Message inMessage = new Message();

        // Path, Method and Headers
        InboundProperty.PATH.set(inMessage, request.uri());
        InboundProperty.METHOD.set(inMessage, request.method().name());
        InboundProperty.HEADERS.set(inMessage, HttpHeadersAsMap.of(request.requestHeaders()));

        // Path Params
        Map<String, String> pathParams = request.params();
        InboundProperty.PATH_PARAMS.set(inMessage, pathParams);

        // Query Params
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> queryParameters = decoder.parameters();
        InboundProperty.QUERY_PARAMS.set(inMessage, queryParameters);

        MimeType payloadMimeType = getContentMimeTypeOrDefault(request, MimeType.TEXT);

        Type type;
        if (payloadMimeType.equals(MimeType.APPLICATION_JSON) || payloadMimeType.equals(MimeType.TEXT)) {
            type = new Type(payloadMimeType, String.class);
        } else {
            type = new Type(payloadMimeType, byte[].class);
        }

        // TODO: NOT Memory typed content. It is flux content
        ByteBufFlux inputFlux = request.receive();
        TypedContent<Flux<ByteBuf>> content = new MemoryTypedContent<>(inputFlux, type);
        inMessage.setTypedContent(content);
        return inMessage;
    }

    private static MimeType getContentMimeTypeOrDefault(HttpServerRequest request, MimeType defaultMimeType) {
        if (request.requestHeaders().contains(HttpHeaderNames.CONTENT_TYPE)) {
            String contentType = request.requestHeaders().get(HttpHeaderNames.CONTENT_TYPE);
            return MimeType.parse(contentType);
        }
        return defaultMimeType;
    }
}
