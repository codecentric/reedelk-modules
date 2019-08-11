package com.reedelk.rest.server;


import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import reactor.netty.http.server.HttpServerRequest;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;


public class HttpPredicate implements Predicate<HttpServerRequest>, Function<Object, Map<String, String>> {

    public static HttpPredicate delete(String uri) {
        return http(uri, null, HttpMethod.DELETE);
    }

    public static HttpPredicate get(String uri) {
        return http(uri, null, HttpMethod.GET);
    }

    public static HttpPredicate head(String uri) {
        return http(uri, null, HttpMethod.HEAD);
    }

    public static HttpPredicate http(String uri, HttpVersion protocol, HttpMethod method) {
        if (null == uri) {
            return null;
        }
        return new HttpPredicate(uri, protocol, method);
    }

    public static HttpPredicate options(String uri) {
        return http(uri, null, HttpMethod.OPTIONS);
    }

    public static HttpPredicate post(String uri) {
        return http(uri, null, HttpMethod.POST);
    }

    public static Predicate<HttpServerRequest> prefix(String prefix) {
        return prefix(prefix, HttpMethod.GET);
    }

    public static Predicate<HttpServerRequest> prefix(String prefix, HttpMethod method) {
        Objects.requireNonNull(prefix, "Prefix must be provided");

        String target = prefix.startsWith("/") ? prefix : "/".concat(prefix);
        return new HttpPredicate.HttpPrefixPredicate(target, method);
    }

    public static HttpPredicate put(String uri) {
        return http(uri, null, HttpMethod.PUT);
    }

    final String uri;
    final HttpMethod method;
    final HttpVersion protocol;
    final UriPathTemplate template;

    @SuppressWarnings("unused")
    public HttpPredicate(String uri) {
        this(uri, null, null);
    }

    public HttpPredicate(String uri, HttpVersion protocol, HttpMethod method) {
        this.protocol = protocol;
        this.uri = uri;
        this.method = method;
        this.template = uri != null ? new UriPathTemplate(uri) : null;
    }

    @Override
    public Map<String, String> apply(Object key) {
        if (template == null) {
            return null;
        }
        Map<String, String> headers = template.match(key.toString());
        if (null != headers && !headers.isEmpty()) {
            return headers;
        }
        return null;
    }

    @Override
    public final boolean test(HttpServerRequest key) {
        return (protocol == null || protocol.equals(key.version())) && (method == null || method.equals(
                key.method())) && (template == null || template.matches(key.uri()));
    }

    static final class HttpPrefixPredicate implements Predicate<HttpServerRequest> {

        final HttpMethod method;
        final String prefix;

        public HttpPrefixPredicate(String prefix, HttpMethod method) {
            this.prefix = prefix;
            this.method = method;
        }

        @Override
        public boolean test(HttpServerRequest key) {
            return (method == null || method.equals(key.method())) && key.uri()
                    .startsWith(
                            prefix);
        }
    }
}

