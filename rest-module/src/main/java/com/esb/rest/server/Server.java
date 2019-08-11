package com.esb.rest.server;


import com.esb.api.component.InboundEventListener;
import com.esb.rest.commons.RestMethod;
import com.esb.rest.component.RestListenerConfiguration;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.TcpServer;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private HttpServerRoutes routes;
    private DisposableServer server;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    Server(RestListenerConfiguration configuration) {
        this.routes = new DefaultServerRoutes();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        TcpServer bootstrap = TcpServer.create()
                .bootstrap(serverBootstrap -> {
                    serverBootstrap.channel(NioServerSocketChannel.class);
                    serverBootstrap.group(bossGroup, workerGroup);
                    return serverBootstrap;
                });

        this.server = HttpServer.from(bootstrap)
                .handle(routes)
                .port(configuration.getPort())
                .host(configuration.getHostname())
                .bindNow();

        requireNonNull(this.server, "server");
    }

    void stop() {
        shutdownSilently(server);
        shutdownGracefully(bossGroup);
        shutdownGracefully(workerGroup);
    }

    public void addRoute(RestMethod method, String path, InboundEventListener listener) {
        requireNonNull(listener, "listener");
        requireNonNull(method, "method");
        requireNonNull(path, "path");

        HttpRequestHandler handler = new HttpRequestHandler(listener);
        method.addRoute(routes, path, handler);
    }

    public void removeRoute(RestMethod method, String path) {
        requireNonNull(method, "method");
        requireNonNull(path, "path");
        routes.remove(HttpMethod.valueOf(method.name()), path);
    }

    boolean hasEmptyRoutes() {
        return routes.isEmpty();
    }

    @Override
    public String toString() {
        return "Server{" +
                "host=" + server.host() +
                ", port=" + server.port() +
                '}';
    }

    private static void shutdownGracefully(EventExecutorGroup executionGroup) {
        try {
            executionGroup.shutdownGracefully(0, 3, SECONDS).sync();
        } catch (InterruptedException e) {
            logger.warn("Error while shutting down event group", e);
        }
    }

    private static void shutdownSilently(DisposableServer server) {
        if (server != null) {
            try {
                server.disposeNow();
            } catch (Exception e) {
                logger.warn("Error while disposing Http server", e);
            }
        }
    }
}
