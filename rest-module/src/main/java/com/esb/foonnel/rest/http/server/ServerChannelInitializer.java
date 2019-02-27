package com.esb.foonnel.rest.http.server;

import com.esb.foonnel.rest.RestListenerConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ServerChannelHandler serverHandler;
    private final RestListenerConfig configuration;

    public ServerChannelInitializer(ServerChannelHandler serverHandler, RestListenerConfig configuration) {
        this.serverHandler = serverHandler;
        this.configuration = configuration;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline();

        p.addLast("readTimeoutHandler", new ReadTimeoutHandler(
                configuration.getReadTimeoutMillis(), MILLISECONDS));

        p.addLast("decoder", new HttpRequestDecoder(
                configuration.getMaxInitialLineLength(),
                configuration.getMaxLengthOfAllHeaders(),
                configuration.getMaxChunkSize(),
                configuration.getValidateHeaders()));

        p.addLast("encoder", new HttpResponseEncoder());

        p.addLast("aggregator", new HttpObjectAggregator(
                configuration.getMaxContentSize()));

        p.addLast(serverHandler);
    }
}
