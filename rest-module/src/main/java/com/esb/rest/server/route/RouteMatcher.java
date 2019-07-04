package com.esb.rest.server.route;

import com.esb.rest.commons.RestMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

public class RouteMatcher {

    private final Routes routes;

    public RouteMatcher(Routes routes) {
        this.routes = routes;
    }

    public Route match(HttpRequest request) {

        RestMethod method = RestMethod.valueOf(request.method().name());

        String uriWithQueryParameters = request.uri();

        // The request uri might contain query parameters.
        // We use the decoder to strip out from the uri
        // the query parameters before matching the route.
        QueryStringDecoder decoder = new QueryStringDecoder(uriWithQueryParameters);

        String path = decoder.path();

        return routes.findRouteOrDefault(method, path);

    }
}
