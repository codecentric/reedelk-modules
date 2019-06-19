package com.esb.rest.server;

import com.esb.rest.component.RestListenerConfiguration;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

import static com.esb.rest.commons.Default.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ServerChannelHandler serverHandler;
    private final RestListenerConfiguration configuration;

    public ServerChannelInitializer(ServerChannelHandler serverHandler, RestListenerConfiguration configuration) {
        this.serverHandler = serverHandler;
        this.configuration = configuration;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline();

        p.addLast("readTimeoutHandler", new ReadTimeoutHandler(
                getValueOrDefault(configuration.getReadTimeoutMillis(), DEFAULT_READ_TIMEOUT_MILLISECONDS),
                MILLISECONDS));

        p.addLast("decoder", new HttpRequestDecoder(
                getValueOrDefault(configuration.getMaxInitialLineLength(), DEFAULT_MAX_INITIAL_LINE_LENGTH),
                getValueOrDefault(configuration.getMaxLengthOfAllHeaders(), DEFAULT_MAX_LENGTH_OF_ALL_HEADERS),
                getValueOrDefault(configuration.getMaxChunkSize(), DEFAULT_MAX_CHUNK_SIZE),
                getValueOrDefault(configuration.getValidateHeaders(), DEFAULT_VALIDATE_HEADERS)));

        p.addLast("encoder", new HttpResponseEncoder());

        p.addLast("aggregator", new HttpObjectAggregator(
                getValueOrDefault(configuration.getMaxContentSize(), DEFAULT_MAX_CONTENT_SIZE)));

        p.addLast(serverHandler);
    }

    private <T> T getValueOrDefault(T original, T defaultValue) {
        return original == null ? defaultValue : original;
    }
}
