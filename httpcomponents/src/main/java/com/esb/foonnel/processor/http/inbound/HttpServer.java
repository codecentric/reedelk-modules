package com.esb.foonnel.processor.http.inbound;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.reactivestreams.Publisher;
import reactor.ipc.netty.http.server.HttpServerRequest;
import reactor.ipc.netty.http.server.HttpServerResponse;
import reactor.ipc.netty.tcp.BlockingNettyContext;

import java.util.function.BiFunction;

public class HttpServer {


    private static MultiKeyMap<String,BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>>> routes =
            MultiKeyMap.multiKeyMap(new HashedMap<>());

    private final reactor.ipc.netty.http.server.HttpServer server;
    private final RouteDispatchHandler handler = new RouteDispatchHandler();

    private BlockingNettyContext serverContext;


    HttpServer(int port, String bindAddress) {
        Preconditions.checkArgument(port >= 0, "port");
        Preconditions.checkArgument(bindAddress != null, "bindAddress");
        server = reactor.ipc.netty.http.server.HttpServer.builder()
                .port(port)
                .bindAddress(bindAddress)
                .build();
    }

    void start() {
         serverContext = server.start(handler);
    }

    void addRoute(String method, String path, BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> handler) {
        if (routes.containsKey(method, path)) {
            throw new IllegalStateException(String.format("A route on this server with method=%s and path=%s is already defined.", method, path));
        }
        routes.put(method, path, handler);
    }

    void removeRoute(String method, String path) {
        routes.remove(method, path);

        // if this is the last route for this server,
        // then we shutdown the socket server.
        serverContext.shutdown();
    }

    class RouteDispatchHandler implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>>  {

        public Publisher<Void> apply(HttpServerRequest request, HttpServerResponse response) {
            String path = request.path();
            HttpMethod method = request.method();
            if (existsRouteHandler(method, path)) {
                return routes.get(method.name(), path).apply(request, response);
            }
            response.status(HttpResponseStatus.NOT_FOUND);
            return response.send();
        }

        private boolean existsRouteHandler(HttpMethod method, String path) {
            return false;
        }
    }
}
