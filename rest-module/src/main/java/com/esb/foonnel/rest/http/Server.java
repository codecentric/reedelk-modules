package com.esb.foonnel.rest.http;


import com.esb.foonnel.rest.RESTConnectionConfiguration;
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

import static io.netty.channel.ChannelOption.*;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final int SHUTDOWN_TIMEOUT = 1;

    private final int port;
    private final String hostname;
    private final Routes routes = new Routes();
    private final ServerBootstrap serverBootstrap;

    private ChannelFuture channelFuture;
    private NioEventLoopGroup bossGroup; // accepts incoming connections.
    private NioEventLoopGroup workerGroup; // handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted connection to the worker.

    public Server(RESTConnectionConfiguration configuration) {
        ServerHandler serverHandler = new ServerHandler(configuration.getProtocol(), routes);
        ServerChannelInitializer channelInitializer = new ServerChannelInitializer(serverHandler, configuration);

        this.port = configuration.getPort();
        this.hostname = configuration.getHostname();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer);

        setChannelOption(SO_BACKLOG, configuration.getSocketBacklog());
        setChannelOption(CONNECT_TIMEOUT_MILLIS, configuration.getConnectionTimeoutMillis());
        setChannelChildOption(SO_KEEPALIVE, configuration.getKeepAlive());
    }

    private <T> void setChannelOption(ChannelOption<T> channelOption, T value) {
        if (value != null) {
            serverBootstrap.option(channelOption, value);
        }
    }

    private <T> void setChannelChildOption(ChannelOption<T> channelOption, T value) {
        if (value != null) {
            serverBootstrap.childOption(channelOption, value);
        }
    }

    public void start() {
        try {
            SocketAddress address = new InetSocketAddress(hostname, port);
            channelFuture = serverBootstrap.bind(address);
        } catch (Exception e) {
            logger.error("server start error", e);
        }
    }

    public void stop() {
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
