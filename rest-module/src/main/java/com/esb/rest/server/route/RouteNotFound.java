package com.esb.rest.server.route;

import com.esb.api.component.OnResult;
import com.esb.api.message.*;
import com.esb.rest.commons.InboundProperty;
import com.esb.rest.commons.OutboundProperty;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

public class RouteNotFound extends Route implements RouteHandler {

    private static final String NOT_FOUND_MESSAGE = "Not Found: for request '%s %s'";

    public RouteNotFound() {
        super();
    }

    @Override
    public RouteHandler handler() {
        return this;
    }

    @Override
    public Map<String, String> bindPathParams(HttpRequest request) {
        return new HashMap<>();
    }

    @Override
    public void handle(Message request, OnResult callback) throws Exception {
        Message response = new Message();

        // The payload is a message containing the Method and Path
        // not found on this server.
        String path = InboundProperty.PATH.getString(request);
        String method = InboundProperty.METHOD.getString(request);
        String message = String.format(NOT_FOUND_MESSAGE, method, path);

        Type contentType = new Type(MimeType.TEXT, String.class);
        TypedContent<String> content = new MemoryTypedContent<>(message, contentType);
        response.setTypedContent(content);

        // Set the response status to 404.
        OutboundProperty.STATUS.set(response, NOT_FOUND.code());

        callback.onResult(response);
    }
}
