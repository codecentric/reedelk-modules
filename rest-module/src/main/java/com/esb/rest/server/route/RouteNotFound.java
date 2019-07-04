package com.esb.rest.server.route;

import com.esb.api.message.*;
import com.esb.rest.commons.InboundProperty;
import com.esb.rest.commons.OutboundProperty;
import com.esb.rest.commons.UriTemplate;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;


public class RouteNotFound extends Route {

    public RouteNotFound() {
        super(new EmptyUriTemplate(), new RouteNotFoundHandler());
    }

    static class RouteNotFoundHandler implements RouteHandler {
        @Override
        public Message handle(Message request) throws Exception {
            String path = InboundProperty.PATH.getString(request);
            String method = InboundProperty.METHOD.getString(request);
            OutboundProperty.STATUS.set(request, NOT_FOUND.code());
            Type contentType = new Type(MimeType.TEXT, String.class);

            String message = "Not Found: for request '" + method + " " + path + "'";
            TypedContent<String> content = new MemoryTypedContent<>(message, contentType);
            request.setTypedContent(content);
            return request;
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
