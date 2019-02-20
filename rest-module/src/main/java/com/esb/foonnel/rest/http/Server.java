package com.esb.foonnel.rest.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final int port;
    private final String hostname;

    private final ServerBootstrap serverBootstrap;

    private final Routes routes;

    private ChannelFuture channelFuture;
    private NioEventLoopGroup bossGroup; // accepts incoming connections.
    private NioEventLoopGroup workerGroup; // handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted connection to the worker.

    public Server(int port, String hostname, ServerChannelInitializer serverChannelInitializer, Routes routes) {
        this.port = port;
        this.routes = routes;
        this.hostname = hostname;

        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(serverChannelInitializer);
    }


    public void start() {
        try {
            // This one can be called many times, with different ports and so on!
            channelFuture = serverBootstrap.bind(new InetSocketAddress(hostname, port));
        } catch (Exception e) {
            logger.error("Server Start", e);
        }
    }

    public void stop() throws InterruptedException {
        bossGroup.shutdownGracefully(0, 1, SECONDS).sync();
        workerGroup.shutdownGracefully(0, 1, SECONDS).sync();
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Server Stop", e);
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
        routes.findRoute(HttpMethod.valueOf(method), path)
                .ifPresent(routes::remove);
    }

    public boolean emptyRoutes() {
        return routes.isEmpty();
    }
}
