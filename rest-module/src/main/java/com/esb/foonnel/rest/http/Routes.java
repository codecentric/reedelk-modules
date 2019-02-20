package com.esb.foonnel.rest.http;

import io.netty.handler.codec.http.HttpMethod;

import java.util.*;

class Routes {

    private final Collection<Route> routes = new ArrayList<>();

    void add(Route route) {
        this.routes.add(route);
    }

    Optional<Route> findRoute(final HttpMethod method, final String path) {
        for (final Route route : routes) {
            if (route.matches(method, path)) {
                return Optional.of(route);
            }
        }

        return Optional.empty();
    }

    void remove(Route route) {
        routes.remove(route);
    }

    boolean isEmpty() {
        return routes.isEmpty();
    }
}
