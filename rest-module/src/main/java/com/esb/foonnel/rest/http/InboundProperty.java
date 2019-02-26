package com.esb.foonnel.rest.http;

import com.esb.foonnel.api.message.InboundProperties;
import com.esb.foonnel.api.message.Message;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.Map;

public enum InboundProperty {

    PATH("path"),
    METHOD("method"),
    HEADERS("headers"),
    PATH_PARAMS("pathParams"),
    QUERY_PARAMS("queryParams");

    String name;

    InboundProperty(String name) {
        this.name = name;
    }

    public void set(Message message, Object value) {
        InboundProperties inboundProperties = message.getInboundProperties();
        inboundProperties.setProperty(name, value);
    }

    public Map<String,String> getMap(Message message) {
        return (Map<String,String>) message.getInboundProperties().getProperty(name);
    }

    public enum Headers {

        CONTENT_TYPE {
            @Override
            public String get(Message message) {
                Map<String, String> headers = InboundProperty.HEADERS.getMap(message);
                return headers.get(HttpHeaderNames.CONTENT_TYPE.toString());
            }
        };

        public abstract String get(Message message);

    }
}
