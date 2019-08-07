package com.esb.rest.server;

import com.esb.api.component.OnResult;
import com.esb.api.message.Message;
import com.esb.rest.commons.HttpProtocol;
import com.esb.rest.server.request.method.MethodStrategyBuilder;
import com.esb.rest.server.route.Route;
import com.esb.rest.server.route.RouteHandler;
import com.esb.rest.server.route.Routes;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

public class ServerChannelHandler extends AbstractServerChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

    private final HttpResponseMapper responseMapper;
    private final Routes routes;

    ServerChannelHandler(HttpProtocol protocol, Routes routes) {
        HttpVersion httpVersion = HttpVersion.valueOf(protocol.get());
        this.responseMapper = new HttpResponseMapper(httpVersion);
        this.routes = routes;
    }

    @Override
    protected void handle(FullHttpRequest request, ChannelHandlerContext context) {

        Route matchingRoute = routes.findRouteOrDefault(request);

        RouteHandler routeHandler = matchingRoute.handler();

        try {
            // Build the 'in' Message object according to the
            // Http method and content type (http strategy)
            Message inMessage = MethodStrategyBuilder
                    .from(request)
                    .execute(request, matchingRoute);

            // invoke the RouteHandler for this Message.
            routeHandler.handle(inMessage, new OnResult() {
                @Override
                public void onResult(Message message) {
                    // Map back the 'out' Message to HTTP Response
                    FullHttpResponse response = responseMapper.map(message);
                    context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }

                @Override
                public void onError(Throwable throwable) {
                    // TODO: Serialize the error
                    FullHttpResponse response = responseMapper.fromStatus(INTERNAL_SERVER_ERROR);
                    context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }
            });

        } catch (Exception exception) {
            logger.error("REST Listener", exception);
            // Map back the 'out' Message to HTTP Response
            FullHttpResponse response = responseMapper.fromStatus(INTERNAL_SERVER_ERROR);
            context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
