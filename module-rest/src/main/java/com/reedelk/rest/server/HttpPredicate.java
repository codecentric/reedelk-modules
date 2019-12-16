package com.reedelk.rest.server;


import com.reedelk.rest.server.uri.UriTemplate;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import reactor.netty.http.server.HttpServerRequest;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class HttpPredicate implements Predicate<HttpServerRequest>, Function<Object, Map<String, String>> {

    final String uri;
    final HttpMethod method;
    private final HttpVersion protocol;
    private final UriTemplate template;

    HttpPredicate(String uri, HttpVersion protocol, HttpMethod method) {
        this.uri = uri;
        this.method = method;
        this.protocol = protocol;
        this.template = uri != null ? new UriTemplate(uri) : null;
    }

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

    public static HttpPredicate put(String uri) {
        return http(uri, null, HttpMethod.PUT);
    }

    @Override
    public Map<String, String> apply(Object key) {
        if (template == null) {
            return null;
        }
        Map<String, String> headers = template.bind(key.toString());
        if (null != headers && !headers.isEmpty()) {
            return headers;
        }
        return null;
    }

    @Override
    public final boolean test(HttpServerRequest key) {
        return (protocol == null || protocol.equals(key.version())) &&
                (method == null || method.equals(key.method())) &&
                (template == null || template.matches(key.uri()));
    }
}

