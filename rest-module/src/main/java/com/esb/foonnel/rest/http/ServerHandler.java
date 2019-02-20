package com.esb.foonnel.rest.http;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.rest.RESTListener;
import com.esb.foonnel.rest.route.Route;
import com.esb.foonnel.rest.route.Routes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;

@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(RESTListener.class);

    private static final String SERVER_NAME = "Foonnel";
    private static final boolean VALIDATE_HEADERS = false;

    private final Routes routeTable;

    public ServerHandler(Routes routeTable) {
        this.routeTable = routeTable;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;
        String uri = request.uri();
        HttpMethod method = request.method();
        HttpHeaders headers = request.headers();

        Optional<Route> route = routeTable.findRoute(method, uri);
        if (!route.isPresent()) {
            writeResponse(context, NOT_FOUND);
            return;
        }

        try {

            String content = request.content().toString(UTF_8);

            Message message = new Message();
            message.setContent(content);
            message.setRequestPath(request.uri());
            message.setRequestMethod(method.name());
            message.setRequestHttpHeaders(requestHeaders(headers));

            Message outMessage = route.get().getHandler().handle(message);
            int httpStatus = outMessage.getHttpStatus();
            boolean hasContentType = outMessage.getResponseHttpHeaders().keySet().contains(CONTENT_TYPE);

            CharSequence contentType = hasContentType ? outMessage.getResponseHttpHeaders().get(CONTENT_TYPE) : TEXT_PLAIN;
            writeResponse(context, valueOf(httpStatus), outMessage.getContent().getBytes(UTF_8), contentType);

        } catch (final Exception exception) {
            logger.error("REST Listener", exception);
            writeResponse(context, INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        ctx.close();
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private static void writeResponse(ChannelHandlerContext context, HttpResponseStatus status) {
        writeResponse(context, status, status.reasonPhrase().getBytes(UTF_8), TEXT_PLAIN);
    }

    private static void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] content, CharSequence contentType) {
        ByteBuf entity = Unpooled.wrappedBuffer(content);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, entity,VALIDATE_HEADERS);

        ZonedDateTime dateTime = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        DefaultHttpHeaders headers = (DefaultHttpHeaders) response.headers();
        headers.set(SERVER, SERVER_NAME);
        headers.set(DATE, dateTime.format(formatter));
        headers.set(CONTENT_TYPE, contentType);
        headers.set(CONTENT_LENGTH, Integer.toString(content.length));

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Map<String,String> requestHeaders(HttpHeaders headers) {
        Map<String,String> requestHeaders = new HashMap<>();
        headers.names().forEach(headerName -> requestHeaders.put(headerName, headers.get(headerName)));
        return requestHeaders;
    }

}
