package com.esb.rest.commons;

import com.esb.api.message.Message;
import com.esb.api.message.MessageProperties;

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

    public String getString(Message message) {
        MessageProperties inboundProperties = message.getInboundProperties();
        return (String) inboundProperties.getProperty(name);
    }

    @SuppressWarnings("unchecked")
    public Map<String,String> getMap(Message message) {
        MessageProperties inboundProperties = message.getInboundProperties();
        return (Map<String, String>) inboundProperties.getProperty(name);
    }
}
