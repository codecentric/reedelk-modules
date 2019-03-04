package com.esb.rest.server;


import com.esb.rest.server.route.RouteHandler;
import com.esb.rest.server.route.Routes;
import com.esb.rest.component.RestListenerConfiguration;
import com.esb.rest.server.route.Route;
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

    private static final int SHUTDOWN_TIMEOUT = 1;
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final int port;
    private final String hostname;
    private final Routes routes = new Routes();
    private final ServerBootstrap serverBootstrap;

    private ChannelFuture channelFuture;
    private NioEventLoopGroup bossGroup; // accepts incoming connections.
    private NioEventLoopGroup workerGroup; // handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted connection to the worker.


    public Server(RestListenerConfiguration configuration) {
        ServerChannelHandler serverHandler = new ServerChannelHandler(configuration.getProtocol(), routes);
        ServerChannelInitializer channelInitializer = new ServerChannelInitializer(serverHandler, configuration);

        this.port = configuration.getPort();
        this.hostname = configuration.getHostname();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer);

        setChannelOption(serverBootstrap, SO_BACKLOG, configuration.getSocketBacklog());
        setChannelOption(serverBootstrap, CONNECT_TIMEOUT_MILLIS, configuration.getConnectionTimeoutMillis());
        setChannelChildOption(serverBootstrap, SO_KEEPALIVE, configuration.getKeepAlive());

        this.serverBootstrap = serverBootstrap;
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

    public void addRoute(String method, String path, RouteHandler routeHandler) {
        routes.add(new Route(method, path, routeHandler));
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

    private static <T> void setChannelOption(ServerBootstrap serverBootstrap, ChannelOption<T> channelOption, T value) {
        if (value != null) {
            serverBootstrap.option(channelOption, value);
        }
    }

    private static <T> void setChannelChildOption(ServerBootstrap serverBootstrap, ChannelOption<T> channelOption, T value) {
        if (value != null) {
            serverBootstrap.childOption(channelOption, value);
        }
    }

}
