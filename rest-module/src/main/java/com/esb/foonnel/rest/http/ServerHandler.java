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
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private final Routes routeTable;

    public ServerHandler(Routes routeTable) {
        this.routeTable = routeTable;
    }


    @Override
    protected FullHttpResponse handle(FullHttpRequest request) {

        Message requestMessage = httpRequestMessageMapper.map(request);

        Optional<Route> route = routeTable.findRoute(
                requestMessage.getRequestMethod(),
                requestMessage.getRequestPath());

        if (!route.isPresent()) return responseWith(NOT_FOUND);

        Route matchingRoute = route.get();

        QueryStringDecoder decoder = new QueryStringDecoder(requestMessage.getRequestPath());
        Map<String, List<String>> requestQueryParams = decoder.parameters();
        requestMessage.setRequestQueryParams(requestQueryParams);

        Map<String, String> requestPathParams = matchingRoute.bindPathParams(requestMessage.getRequestPath());
        requestMessage.setRequestPathParams(requestPathParams);


        try {
            // Foonnel Runtime Executes the graph
            Message outMessage = matchingRoute.handler().handle(requestMessage);
            return messageHttpResponseMapper.map(outMessage);

        } catch (final Exception exception) {
            logger.error("REST Listener", exception);
            return responseWith(INTERNAL_SERVER_ERROR);
        }
    }

    private FullHttpResponse responseWith(HttpResponseStatus status) {
        String content = status.reasonPhrase();
        ByteBuf entity = Unpooled.wrappedBuffer(content.getBytes(UTF_8));

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, entity);
        response.headers().add(CONTENT_TYPE, TEXT_PLAIN);
        return response;
    }

}
