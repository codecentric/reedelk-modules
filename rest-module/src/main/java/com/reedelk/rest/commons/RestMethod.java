package com.reedelk.rest.commons;

import com.reedelk.rest.server.HttpRequestHandler;
import com.reedelk.rest.server.HttpServerRoutes;

public enum RestMethod {
    GET {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.get(path, handler);
        }

        @Override
        public boolean hasBody() {
            return false;
        }
    },
    POST {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.post(path, handler);
        }

        @Override
        public boolean hasBody() {
            return true;
        }
    },
    PUT {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.put(path, handler);
        }

        @Override
        public boolean hasBody() {
            return true;
        }
    },
    DELETE {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.delete(path, handler);
        }

        @Override
        public boolean hasBody() {
            return true;
        }
    },
    HEAD {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.head(path, handler);
        }

        @Override
        public boolean hasBody() {
            return false;
        }
    },
    OPTIONS {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.options(path, handler);
        }

        @Override
        public boolean hasBody() {
            return false;
        }
    };

    public abstract boolean hasBody();

    public abstract void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler);

}
