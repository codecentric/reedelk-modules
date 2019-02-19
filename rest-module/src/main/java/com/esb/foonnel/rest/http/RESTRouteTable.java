package com.esb.foonnel.rest.http;

import io.netty.handler.codec.http.HttpMethod;

import java.util.*;

public class RESTRouteTable {

    private final List<RESTRoute> routes = new ArrayList<>();


    public void add(RESTRoute route) {
        this.routes.add(route);
    }

    public Collection<RESTRoute> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    public Optional<RESTRoute> findRoute(final HttpMethod method, final String path) {
        for (final RESTRoute route : routes) {
            if (route.matches(method, path)) {
                return Optional.of(route);
            }
        }

        return Optional.empty();
    }
}
