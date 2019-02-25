package com.esb.foonnel.rest.http;

import com.esb.foonnel.api.InboundProperties;
import com.esb.foonnel.api.Message;

public enum InboundProperty {

    PATH("path"),
    METHOD("method"),
    HEADERS("headers"),
    QUERY_PARAMS("queryParams"),
    PATH_PARAMS("pathParams");

    String name;

    InboundProperty(String name) {
        this.name = name;
    }

    public void set(Message message, Object value) {
        InboundProperties inboundProperties = message.getInboundProperties();
        inboundProperties.setProperty(name, value);
    }

    public Object get(InboundProperties properties) {
        return properties.getProperty(name);
    }
}
