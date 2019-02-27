package com.esb.foonnel.rest.http.server.request.body;

import com.esb.foonnel.rest.http.server.request.method.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpMethod.DELETE;

public class BodyStrategy {

    private static final Map<HttpMethod, RequestStrategy> STRATEGIES;

    static {
        STRATEGIES = new HashMap<>();
        STRATEGIES.put(GET, new GetRequest());
        STRATEGIES.put(PUT, new PutRequest());
        STRATEGIES.put(POST, new PostRequest());
        STRATEGIES.put(DELETE, new DeleteRequest());
    }

    public static RequestStrategy from(HttpRequest request) {
        if (!STRATEGIES.containsKey(request.method())) {
            throw new IllegalStateException("Could not find strategy for: " + request.method());
        }
        return STRATEGIES.get(request.method());
    }

}
