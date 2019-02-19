package com.esb.foonnel.rest.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

public class RESTServer {

    private final int port;
    private final String hostname;

    private final Routes routes;

    private ChannelFuture channelFuture;
    private NioEventLoopGroup bossGroup; // accepts incoming connections.
    private NioEventLoopGroup workerGroup; // handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted connection to the worker.

    public RESTServer(int port, String hostname) {
        this.port = port;
        this.hostname = hostname;
        this.routes = new Routes();
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
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new RESTServerInitializer(routes));

            // This one can be called many times, with different ports and so on!
            channelFuture = serverBootstrap.bind(new InetSocketAddress(hostname, port));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() throws InterruptedException {
        if (!routes.isEmpty()) return;

        bossGroup.shutdownGracefully(0, 1, SECONDS).sync();
        workerGroup.shutdownGracefully(0, 1, SECONDS).sync();
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

    public void addRoute(String method, String path, Handler handler) {
        routes.add(new Route(HttpMethod.valueOf(method), path, handler));
    }

    public void removeRoute(String method, String path) {
        routes
                .findRoute(HttpMethod.valueOf(method), path)
                .ifPresent(routes::remove);
    }
}
