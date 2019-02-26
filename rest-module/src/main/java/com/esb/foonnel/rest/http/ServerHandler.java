package com.esb.foonnel.rest.http;

import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.rest.http.strategies.HttpStrategy;
import com.esb.foonnel.rest.route.Route;
import com.esb.foonnel.rest.route.Routes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ServerHandler extends AbstractServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final Routes routes;
    private final HttpVersion httpVersion;

    public ServerHandler(String protocol, Routes routes) {
        this.routes = routes;
        this.httpVersion = HttpVersion.valueOf(protocol);
    }

    @Override
    protected FullHttpResponse handle(FullHttpRequest request) {

        HttpMethod method = request.method();
        String uri = request.uri();

        Optional<Route> optionalMatchingPath = routes.findRoute(method.name(), uri);

        if (!optionalMatchingPath.isPresent()) {
            return responseWith(NOT_FOUND);
        }

        Route matchingPath = optionalMatchingPath.get();

        try {
            // Build Foonnel according to the HTTP strategy.
            Message inMessage = HttpStrategy.from(request).handle(request, matchingPath);

            // Run through the Foonnel flow the message
            Message outMessage = matchingPath.handler().handle(inMessage);

            // Map the returned (processed) message to be sent back as HTTP Response.
            return asHttpResponse(outMessage);

        } catch (Exception exception) {
            logger.error("REST Listener", exception);
            return responseWith(INTERNAL_SERVER_ERROR);
        }
    }

    private FullHttpResponse responseWith(HttpResponseStatus status) {
        String content = status.reasonPhrase();
        byte[] bytes = content.getBytes(UTF_8);
        ByteBuf entity = Unpooled.wrappedBuffer(bytes);

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpVersion, status, entity);

        HttpHeaders headers = response.headers();
        headers.add(CONTENT_TYPE, TEXT_PLAIN);
        headers.add(CONTENT_LENGTH, bytes.length);
        return response;
    }

    private FullHttpResponse asHttpResponse(Message outMessage) {
        int httpStatus = OutboundProperty.STATUS.getInt(outMessage);

        byte[] bytes = new byte[0];
        if (outMessage.getContent().getType().getTypeClass().isAssignableFrom(byte[].class)) {
            bytes = (byte[]) outMessage.getContent().getContent();
        }
        if (outMessage.getContent().getType().getTypeClass().isAssignableFrom(String.class)) {
            bytes = ((String) outMessage.getContent().getContent()).getBytes();
        }

        ByteBuf entity = Unpooled.wrappedBuffer(bytes);

        Map<String, String> outboundHeaders = OutboundProperty.HEADERS.getMap(outMessage);
        boolean hasContentType = outboundHeaders.containsKey(CONTENT_TYPE.toString());
        CharSequence contentType = hasContentType ? outboundHeaders.get(CONTENT_TYPE.toString()) : TEXT_PLAIN;

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpVersion, valueOf(httpStatus), entity);

        HttpHeaders headers = response.headers();
        headers.add(CONTENT_TYPE, contentType);
        headers.add(CONTENT_LENGTH, bytes.length);

        for (Map.Entry<String,String> header : outboundHeaders.entrySet()) {
            headers.add(header.getKey(), header.getValue());
        }

        return response;
    }

}
