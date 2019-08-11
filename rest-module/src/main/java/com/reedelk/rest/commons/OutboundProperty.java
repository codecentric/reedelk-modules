package com.reedelk.rest.commons;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageProperties;

import java.io.Serializable;
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

    public void set(Message message, Serializable value) {
        MessageProperties outboundProperties = message.getOutboundProperties();
        outboundProperties.setProperty(name, value);
    }

}
