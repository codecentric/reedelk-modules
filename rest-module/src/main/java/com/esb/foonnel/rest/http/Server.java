package com.esb.foonnel.rest.http;


import com.esb.foonnel.rest.route.Route;
import com.esb.foonnel.rest.route.Routes;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int SHUTDOWN_TIMEOUT = 1;

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
            SocketAddress address = new InetSocketAddress(hostname, port);
            channelFuture = serverBootstrap.bind(address);
        } catch (Exception e) {
            logger.error("server start error", e);
        }
    }

    public void stop() throws InterruptedException {
        shutdownGracefully(bossGroup);
        shutdownGracefully(workerGroup);
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("server stop error", e);
        } finally {
            bossGroup = null;
            workerGroup = null;
            channelFuture = null;
        }
    }

    public void addRoute(String method, String path, Handler handler) {
        routes.add(new Route(method, path, handler));
    }

    public void removeRoute(String method, String path) {
        routes.findRoute(method, path)
                .ifPresent(routes::remove);
    }

    public boolean emptyRoutes() {
        return routes.isEmpty();
    }

    private void shutdownGracefully(EventExecutorGroup executionGroup) {
        try {
            executionGroup.shutdownGracefully(0, SHUTDOWN_TIMEOUT, SECONDS).sync();
        } catch (InterruptedException e) {
            logger.error("Executor Group Shutdown Error", e);
        }
    }

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }
}
