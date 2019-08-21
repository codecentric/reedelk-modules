package com.reedelk.rest.server;


import com.reedelk.rest.commons.StringUtils;
import com.reedelk.rest.configuration.RestListenerConfiguration;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.component.InboundEventListener;
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

    private final RestListenerConfiguration configuration;

    Server(RestListenerConfiguration configuration) {
        this.configuration = configuration;

        this.routes = new DefaultServerRoutes();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        TcpServer bootstrap = createTcpServer(configuration);
        HttpServer httpServer = HttpServer.from(bootstrap).handle(routes);
        this.server = ServerConfigurer.configure(httpServer, configuration).bindNow();
    }

    public String getBasePath() {
        return configuration.getBasePath();
    }

    public void addRoute(RestMethod method, String path, InboundEventListener listener) {
        requireNonNull(listener, "listener");
        requireNonNull(method, "method");
        requireNonNull(path, "path");

        String realPath = getRealPath(path);

        HttpRequestHandler handler = new HttpRequestHandler(listener);
        method.addRoute(routes, realPath, handler);
    }

    public void removeRoute(RestMethod method, String path) {
        requireNonNull(method, "method");
        requireNonNull(path, "path");

        String realPath = getRealPath(path);

        routes.remove(HttpMethod.valueOf(method.name()), realPath);
    }

    void stop() {
        shutdownSilently(server);
        shutdownGracefully(bossGroup);
        shutdownGracefully(workerGroup);
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

    private TcpServer createTcpServer(RestListenerConfiguration configuration) {
        TcpServer bootstrap = TcpServer.create();
        bootstrap = ServerConfigurer.configureSecurity(bootstrap, configuration);
        bootstrap = bootstrap
                .bootstrap(serverBootstrap -> {
                    ServerConfigurer.configure(serverBootstrap, configuration);
                    return serverBootstrap
                            .channel(NioServerSocketChannel.class)
                            .group(bossGroup, workerGroup);
                })
                .doOnConnection(ServerConfigurer.onConnection(configuration));
        return bootstrap;
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

    /**
     * Returns the real path, with the base path prefixed to the given
     * path if it is not blank.
     * @param path the original path.
     * @return the base path + original path if the base path is not blank,
     * otherwise the original path is returned.
     */
    private String getRealPath(String path) {
        return StringUtils.isNotBlank(configuration.getBasePath()) ?
                configuration.getBasePath() + path :
                path;
    }
}
