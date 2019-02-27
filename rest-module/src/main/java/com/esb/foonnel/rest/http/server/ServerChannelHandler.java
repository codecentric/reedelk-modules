package com.esb.foonnel.rest.http.server;

import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.rest.commons.OutboundProperty;
import com.esb.foonnel.rest.http.server.request.method.MethodStrategyBuilder;
import com.esb.foonnel.rest.http.server.request.method.MethodStrategy;
import com.esb.foonnel.rest.http.server.route.Route;
import com.esb.foonnel.rest.http.server.route.RouteHandler;
import com.esb.foonnel.rest.http.server.route.Routes;
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

public class ServerChannelHandler extends AbstractServerChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

    private final Routes routes;
    private final HttpVersion httpVersion;

    public ServerChannelHandler(String protocol, Routes routes) {
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
        RouteHandler routeHandler = matchingPath.handler();
        try {
            // Build Foonnel Message according to the method and content type (http strategy)
            MethodStrategy strategy = MethodStrategyBuilder.from(request);
            Message inMessage = strategy.execute(request, matchingPath);

            // Call the Route handler for this Message.
            Message outMessage = routeHandler.handle(inMessage);

            // Map the returned (processed) message to be sent back as HTTP Response.
            return asHttpResponse(outMessage);

        } catch (Exception exception) {
            logger.error("REST Listener", exception);
            return responseWith(INTERNAL_SERVER_ERROR);
        }
    }

    // Response Mapper
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

    // Response Mapper
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
