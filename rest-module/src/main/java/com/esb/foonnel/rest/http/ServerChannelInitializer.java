package com.esb.foonnel.rest.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final AbstractServerHandler serverHandler;

    public ServerChannelInitializer(AbstractServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline p = ch.pipeline();
        // TODO: Check these values!!!!
        p.addLast(new HttpRequestDecoder(4096, 8192, 8192, false));
        p.addLast(new HttpResponseEncoder());
        p.addLast(new HttpObjectAggregator(100 * 1024 * 1024));
        p.addLast(serverHandler);
    }
}
