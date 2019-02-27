package com.esb.foonnel.rest.http.request.method;

import com.esb.foonnel.api.message.*;
import com.esb.foonnel.rest.commons.HeadersUtils;
import com.esb.foonnel.rest.commons.InboundProperty;
import com.esb.foonnel.rest.http.route.Route;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractStrategy implements RequestStrategy {

    @Override
    public Message execute(FullHttpRequest request, Route matchingRoute) throws Exception {
        Message inMessage = new Message();

        // Path, Method and Headers
        InboundProperty.PATH.set(inMessage, request.uri()); //TODO: Request path not request.uri
        InboundProperty.METHOD.set(inMessage, request.method().name());
        InboundProperty.HEADERS.set(inMessage, HeadersUtils.toMap(request.headers()));

        // Path Params
        Map<String, String> pathParams = matchingRoute.bindPathParams(request.uri());
        InboundProperty.PATH_PARAMS.set(inMessage, pathParams);

        // Query Params
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> queryParams = decoder.parameters();
        InboundProperty.QUERY_PARAMS.set(inMessage, queryParams);

        return handle0(inMessage, request);
    }

    protected Message handle0(Message inMessage, FullHttpRequest request) throws IOException {
        return inMessage;
    }

    protected TypedContent<byte[]> extractBodyContent(Message inMessage, FullHttpRequest request) {
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

        Type type = new Type(MimeType.parse(contentType), byte[].class);
        return new MemoryTypedContent<>(bytes, type);
    }

}
