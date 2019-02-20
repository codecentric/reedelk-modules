package com.esb.foonnel.rest.route;

import com.esb.foonnel.rest.commons.UriTemplate;
import com.esb.foonnel.rest.http.Handler;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;

public class Route {

    private final String path;
    private final Handler handler;
    private final HttpMethod method;
    private final UriTemplate uriTemplate;

    public Route(HttpMethod method, String uriTemplate, Handler handler) {
        this.method = method;
        this.path = uriTemplate;
        this.handler = handler;
        this.uriTemplate = new UriTemplate(uriTemplate);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Handler getHandler() {
        return handler;
    }

    public boolean matches(HttpMethod method, String path) {
        return this.method.equals(method) && this.uriTemplate.matches(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return method.equals(route.method) &&
                path.equals(route.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}
