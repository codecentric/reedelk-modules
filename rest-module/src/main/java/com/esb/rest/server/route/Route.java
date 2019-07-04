package com.esb.rest.server.route;

import com.esb.rest.commons.RestMethod;
import com.esb.rest.commons.UriTemplate;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

import static com.esb.rest.commons.Preconditions.isNotNull;

public class Route {

    private final RestMethod method;
    private final UriTemplate uriTemplate;
    private final RouteHandler routeHandler;

    public Route(RestMethod method, String uriTemplate, RouteHandler routeHandler) {
        isNotNull(method, "method");
        isNotNull(uriTemplate, "uriTemplate");
        isNotNull(routeHandler, "routeHandler");

        this.method = method;
        this.routeHandler = routeHandler;
        this.uriTemplate = new UriTemplate(uriTemplate);
    }

    protected Route() {
        this.method = null;
        this.uriTemplate = null;
        this.routeHandler = null;
    }

    public RestMethod getMethod() {
        return method;
    }

    public RouteHandler handler() {
        return routeHandler;
    }

    boolean matches(HttpRequest request) {
        RestMethod method = RestMethod.valueOf(request.method().name());

        String uriWithQueryParameters = request.uri();

        // The request uri might contain query parameters.
        // We use the decoder to strip out from the uri
        // the query parameters before matching the route.
        QueryStringDecoder decoder = new QueryStringDecoder(uriWithQueryParameters);

        String path = decoder.path();

        return matches(method, path);
    }

    boolean matches(RestMethod method, String path) {
        return this.method.equals(method) &&
                this.uriTemplate.matches(path);
    }

    public Map<String, String> bindPathParams(HttpRequest request) {
        return this.uriTemplate.bind(request.uri());
    }

    public Map<String, List<String>> queryParameters(HttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        return decoder.parameters();
    }
}
