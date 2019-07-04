package com.esb.rest.server.route;

import com.esb.api.message.*;
import com.esb.rest.commons.InboundProperty;
import com.esb.rest.commons.OutboundProperty;
import com.esb.rest.commons.UriTemplate;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;


public class RouteNotFound extends Route {

    private static final String NOT_FOUND_MESSAGE = "Not Found: for request '%s %s'";

    public RouteNotFound() {
        super(new EmptyUriTemplate(), new RouteNotFoundHandler());
    }

    static class RouteNotFoundHandler implements RouteHandler {
        @Override
        public Message handle(Message request) throws Exception {

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

            return response;
        }
    }

    static class EmptyUriTemplate extends UriTemplate {

        @Override
        public boolean matches(String uri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String> bind(String uri) {
            return new HashMap<>();
        }
    }
}
