package com.reedelk.rest.configuration;

import com.reedelk.rest.server.HttpRequestHandler;
import com.reedelk.rest.server.HttpServerRoutes;
import com.reedelk.runtime.api.annotation.Default;
import reactor.netty.http.client.HttpClient;

import static reactor.netty.http.client.HttpClient.ResponseReceiver;

@Default("GET")
public enum RestMethod {

    GET {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.get(path, handler);
        }

        @Override
        public ResponseReceiver addForClient(HttpClient client) {
            return client.get();
        }
    },

    POST {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.post(path, handler);
        }

        @Override
        public ResponseReceiver addForClient(HttpClient client) {
            return client.post();
        }


    },

    PUT {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.put(path, handler);
        }

        @Override
        public ResponseReceiver addForClient(HttpClient client) {
            return client.put();
        }
    },

    DELETE {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.delete(path, handler);
        }

        @Override
        public ResponseReceiver addForClient(HttpClient client) {
            return client.delete();
        }
    },

    HEAD {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.head(path, handler);
        }

        @Override
        public ResponseReceiver addForClient(HttpClient client) {
            return client.head();
        }
    },

    OPTIONS {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.head(path, handler);
        }

        @Override
        public ResponseReceiver addForClient(HttpClient client) {
            return client.options();
        }
    };

    public abstract void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler);

    public abstract ResponseReceiver addForClient(HttpClient client);

}
