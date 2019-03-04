package com.esb.rest.rest.commons;

import com.esb.api.message.Message;
import com.esb.api.message.MessageProperties;
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
        MessageProperties inboundProperties = message.getInboundProperties();
        inboundProperties.setProperty(name, value);
    }

    @SuppressWarnings("unchecked")
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
