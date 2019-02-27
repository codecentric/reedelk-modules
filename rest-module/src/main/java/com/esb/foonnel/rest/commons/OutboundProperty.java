package com.esb.foonnel.rest.commons;

import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.api.message.MessageProperties;

import java.util.Map;

public enum OutboundProperty {

    STATUS("status"),
    HEADERS("headers");

    String name;

    OutboundProperty(String name) {
        this.name = name;
    }

    public int getInt(Message message) {
        return (Integer) get(message);
    }

    @SuppressWarnings("unchecked")
    public Map<String,String> getMap(Message message) {
        return (Map<String,String>) get(message);
    }

    public Object get(Message message) {
        MessageProperties outboundProperties = message.getOutboundProperties();
        return outboundProperties.getProperty(name);
    }

    public void set(Message message, Object value) {
        MessageProperties outboundProperties = message.getOutboundProperties();
        outboundProperties.setProperty(name, value);
    }

}
