package com.esb.foonnel.rest.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    public static final String TYPE_PLAIN = "text/plain; charset=UTF-8";
    public static final String TYPE_JSON = "application/json; charset=UTF-8";
    public static final String SERVER_NAME = "Foonnel";

    private final Routes routeTable;

    public ServerHandler(Routes routeTable) {
        this.routeTable = routeTable;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }

        final FullHttpRequest request = (FullHttpRequest) msg;
        final HttpMethod method = request.method();
        final String uri = request.uri();

        final Optional<Route> route = routeTable.findRoute(method, uri);
        if (!route.isPresent()) {
            writeNotFound(ctx, request);
            return;
        }

        try {
            final Request requestWrapper = new Request(request);
            final Response response = route.get().getHandler().handle(requestWrapper);
            String content = response.response.content().toString(StandardCharsets.UTF_8);

            writeResponse(ctx, request, OK, TYPE_JSON, content);
        } catch (final Exception ex) {
            ex.printStackTrace();
            writeInternalServerError(ctx, request);
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

    private static void writeNotFound(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        writeErrorResponse(ctx, request, NOT_FOUND);
    }

    private static void writeInternalServerError(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        writeErrorResponse(ctx, request, INTERNAL_SERVER_ERROR);
    }

    private static void writeErrorResponse(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        writeResponse(ctx, request, status, TYPE_PLAIN, status.reasonPhrase());
    }

    private static void writeResponse(ChannelHandlerContext ctx,FullHttpRequest request, HttpResponseStatus status, CharSequence contentType, String content) {
        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        final ByteBuf entity = Unpooled.wrappedBuffer(bytes);
        writeResponse(ctx, request, status, entity, contentType, bytes.length);
    }

    private static void writeResponse(
            final ChannelHandlerContext ctx,
            final FullHttpRequest request,
            final HttpResponseStatus status,
            final ByteBuf buf,
            final CharSequence contentType,
            final int contentLength) {

        // Decide whether to close the connection or not.
        final boolean keepAlive = false;

        // Build the response object.
        final FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                status,
                buf,
                false);

        final ZonedDateTime dateTime = ZonedDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

        final DefaultHttpHeaders headers = (DefaultHttpHeaders) response.headers();
        headers.set(HttpHeaderNames.SERVER, SERVER_NAME);
        headers.set(HttpHeaderNames.DATE, dateTime.format(formatter));
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
        headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(contentLength));

        // Close the non-keep-alive connection after the write operation is done.
        if (!keepAlive) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.writeAndFlush(response, ctx.voidPromise());
        }
    }

    private static void send100Continue(final ChannelHandlerContext ctx) {
        ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
    }
}
