package com.esb.rest.commons;

import com.esb.rest.server.HttpRequestHandler;
import com.esb.rest.server.HttpServerRoutes;

public enum RestMethod {

    GET {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.get(path, handler);
        }
    },
    POST {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.post(path, handler);
        }
    },
    PUT {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.put(path, handler);
        }
    },
    DELETE {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.delete(path, handler);
        }
    },
    HEAD {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.head(path, handler);
        }
    },
    OPTIONS {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.head(path, handler);
        }
    };

    public abstract void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler);


}
