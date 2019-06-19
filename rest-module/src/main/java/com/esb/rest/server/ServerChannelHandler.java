package com.esb.rest.server;

import com.esb.api.message.Message;
import com.esb.rest.commons.HttpProtocol;
import com.esb.rest.commons.RestMethod;
import com.esb.rest.server.request.method.MethodStrategy;
import com.esb.rest.server.request.method.MethodStrategyBuilder;
import com.esb.rest.server.route.Route;
import com.esb.rest.server.route.RouteHandler;
import com.esb.rest.server.route.Routes;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

public class ServerChannelHandler extends AbstractServerChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

    private final Routes routes;
    private final HttpVersion httpVersion;
    private final HttpResponseMapper responseMapper;

    public ServerChannelHandler(HttpProtocol protocol, Routes routes) {
        this.routes = routes;
        this.httpVersion = HttpVersion.valueOf(protocol.get());
        this.responseMapper = new HttpResponseMapper(httpVersion);
    }

    @Override
    protected FullHttpResponse handle(FullHttpRequest request) {

        RestMethod method = RestMethod.valueOf(request.method().name());
        String uri = request.uri();

        Optional<Route> optionalMatchingPath = routes.findRoute(method, uri);

        if (!optionalMatchingPath.isPresent()) {
            return responseMapper.fromStatus(NOT_FOUND);
        }

        Route matchingPath = optionalMatchingPath.get();
        RouteHandler routeHandler = matchingPath.handler();

        try {
            // Build Message according to the method and content type (http strategy)
            MethodStrategy strategy = MethodStrategyBuilder.from(request);
            Message inMessage = strategy.execute(request, matchingPath);

            // invoke the RouteHandler for this Message.
            Message outMessage = routeHandler.handle(inMessage);

            // Map Message to HTTP Response.
            return responseMapper.map(outMessage);

        } catch (Exception exception) {
            logger.error("REST Listener", exception);
            return responseMapper.fromStatus(INTERNAL_SERVER_ERROR);
        }
    }


}
