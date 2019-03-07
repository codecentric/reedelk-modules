package com.esb.rest.server.route;

import com.esb.rest.commons.RestMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class Routes {

    private final Collection<Route> routes = new ArrayList<>();

    public void add(Route route) {
        this.routes.add(route);
    }

    public Optional<Route> findRoute(final RestMethod method, final String path) {
        for (final Route route : routes) {
            if (route.matches(method, path)) {
                return Optional.of(route);
            }
        }
        return Optional.empty();
    }

    public void remove(Route route) {
        routes.remove(route);
    }

    public boolean isEmpty() {
        return routes.isEmpty();
    }
}