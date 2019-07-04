package com.esb.rest.server.route;

import com.esb.rest.commons.RestMethod;
import com.esb.rest.commons.UriTemplate;

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

    boolean matches(RestMethod method, String path) {
        return this.method.equals(method) &&
                this.uriTemplate.matches(path);
    }

    public Map<String,String> bindPathParams(String requestUri) {
        return this.uriTemplate.bind(requestUri);
    }
}
