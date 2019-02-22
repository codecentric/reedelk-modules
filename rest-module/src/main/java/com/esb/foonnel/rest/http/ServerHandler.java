package com.esb.foonnel.rest.http;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.rest.mapper.HttpRequestToMessage;
import com.esb.foonnel.rest.mapper.Mapper;
import com.esb.foonnel.rest.mapper.MessageToHttpResponse;
import com.esb.foonnel.rest.route.Route;
import com.esb.foonnel.rest.route.Routes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ServerHandler extends AbstractServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final Mapper<FullHttpRequest,Message> httpRequestMessageMapper = new HttpRequestToMessage();
    private final Mapper<Message, FullHttpResponse> messageHttpResponseMapper = new MessageToHttpResponse();

    private final Routes routesRegistry;

    public ServerHandler(Routes routesRegistry) {
        this.routesRegistry = routesRegistry;
    }


    @Override
    protected FullHttpResponse handle(FullHttpRequest request) {

        Message inMessage = httpRequestMessageMapper.map(request);

        Optional<Route> route = routesRegistry.findRoute(inMessage.getRequestMethod(), inMessage.getRequestPath());

        if (!route.isPresent()) return responseWith(NOT_FOUND);

        Route matchingRoute = route.get();

        QueryStringDecoder decoder = new QueryStringDecoder(inMessage.getRequestPath());
        Map<String, List<String>> requestQueryParams = decoder.parameters();
        inMessage.setRequestQueryParams(requestQueryParams);

        Map<String, String> requestPathParams = matchingRoute.bindPathParams(inMessage.getRequestPath());
        inMessage.setRequestPathParams(requestPathParams);

        try {
            // Foonnel Runtime Executes the graph
            Message outMessage = matchingRoute.handler().handle(inMessage);
            return messageHttpResponseMapper.map(outMessage);

        } catch (Exception exception) {
            logger.error("REST Listener", exception);
            return responseWith(INTERNAL_SERVER_ERROR);
        }
    }

    private FullHttpResponse responseWith(HttpResponseStatus status) {
        String content = status.reasonPhrase();
        byte[] bytes = content.getBytes(UTF_8);
        ByteBuf entity = Unpooled.wrappedBuffer(bytes);

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, entity);
        HttpHeaders headers = response.headers();
        headers.add(CONTENT_TYPE, TEXT_PLAIN);
        headers.add(CONTENT_LENGTH, bytes.length);
        return response;
    }

}
