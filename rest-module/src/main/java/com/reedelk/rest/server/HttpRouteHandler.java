package com.reedelk.rest.server;

import io.netty.handler.codec.http.HttpMethod;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class HttpRouteHandler implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>>, Predicate<HttpServerRequest> {

    private final HttpPredicate condition;
    private final Function<? super String, Map<String, String>> resolver;
    private final BiFunction<? super HttpServerRequest, ? super HttpServerResponse, ? extends Publisher<Void>> handler;

    public HttpRouteHandler(HttpPredicate condition,
                            BiFunction<? super HttpServerRequest, ? super HttpServerResponse, ? extends Publisher<Void>> handler,
                            Function<? super String, Map<String, String>> resolver) {
        this.condition = requireNonNull(condition, "condition");
        this.handler = requireNonNull(handler, "handler");
        this.resolver = resolver;
    }

    @Override
    public Publisher<Void> apply(HttpServerRequest request, HttpServerResponse response) {
        return handler.apply(request.paramsResolver(resolver), response);
    }

    @Override
    public boolean test(HttpServerRequest request) {
        return condition.test(request);
    }

    public boolean matchesExactly(HttpMethod method, String uri) {
        requireNonNull(method, "method");
        requireNonNull(uri, "uri");
        return method.equals(condition.method) && uri.equals(condition.uri);
    }
}
