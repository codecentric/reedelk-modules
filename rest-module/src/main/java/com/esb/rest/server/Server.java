package com.esb.rest.server;


import com.esb.api.component.InboundEventListener;
import com.esb.rest.commons.RestMethod;
import com.esb.rest.component.RestListenerConfiguration;
import io.netty.handler.codec.http.HttpMethod;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import static java.util.Objects.requireNonNull;

public class Server {

    private HttpServerRoutes routes;
    private DisposableServer server;

    Server(RestListenerConfiguration configuration) {
        int port = configuration.getPort();
        String bindAddress = configuration.getHostname();

        routes = new DefaultServerRoutes();
        server = HttpServer.create()
                .handle(routes)
                .port(port)
                .host(bindAddress)
                .bindNow();
    }

    public void addRoute(RestMethod method, String path, InboundEventListener listener) {
        requireNonNull(listener, "listener");
        requireNonNull(method, "method");
        requireNonNull(path, "path");
        HttpRequestHandler requestHandler = new HttpRequestHandler(listener);
        method.addRoute(routes, path, requestHandler);
    }

    public void removeRoute(RestMethod method, String path) {
        requireNonNull(method, "method");
        requireNonNull(path, "path");
        routes.remove(HttpMethod.valueOf(method.name()), path);
    }

    void stop() {
        if (server != null) {
            server.dispose();
        }
    }

    boolean hasNoRoutes() {
        return routes.isEmpty();
    }
}
