package com.esb.rest.commons;

import com.esb.api.message.Message;
import com.esb.api.message.MessageProperties;

import java.util.Map;

public enum OutboundProperty {

    STATUS("status"),
    HEADERS("headers");

    String name;

    OutboundProperty(String name) {
        this.name = name;
    }

    public int getInt(Message message) {
        return (int) get(message);
    }

    public boolean isDefined(Message message) {
        MessageProperties outboundProperties = message.getOutboundProperties();
        return outboundProperties.containsKey(name);
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
