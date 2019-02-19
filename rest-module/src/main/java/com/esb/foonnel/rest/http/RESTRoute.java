package com.esb.foonnel.rest.http;

import io.netty.handler.codec.http.HttpMethod;

public class RESTRoute {

    private final HttpMethod method;
    private final String path;
    private final Handler handler;

    public RESTRoute(HttpMethod method, String path, Handler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
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
        return this.method.equals(method) && this.path.equals(path);
    }
}
