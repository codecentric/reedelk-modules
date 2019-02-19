package com.esb.foonnel.rest.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class RESTServer {

    private final int port;
    private final String hostname;

    private final RESTRouteTable routeTable;

    private ChannelFuture channelFuture;
    private NioEventLoopGroup bossGroup; // accepts incoming connections.
    private NioEventLoopGroup workerGroup; // handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted connection to the worker.

    public RESTServer(int port, String hostname) {
        this.port = port;
        this.hostname = hostname;
        this.routeTable = new RESTRouteTable();
    }

    public void start() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            // This is generic and should be extracted into a pool
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // (6)
                    .childHandler(new RESTServerInitializer());

            // This one can be called many times, with different ports and so on!
            channelFuture = serverBootstrap.bind(new InetSocketAddress(hostname, port));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() throws InterruptedException {
        bossGroup.shutdownGracefully(0, 1, TimeUnit.SECONDS).sync();
        workerGroup.shutdownGracefully(0, 1, TimeUnit.SECONDS).sync();
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup = null;
            workerGroup = null;
            channelFuture = null;
        }
    }

    public RESTServer get(final String path, final Handler handler) {
        this.routeTable.add(new RESTRoute(HttpMethod.GET, path, handler));
        return this;
    }

    public RESTServer post(final String path, final Handler handler) {
        this.routeTable.add(new RESTRoute(HttpMethod.POST, path, handler));
        return this;
    }

}
