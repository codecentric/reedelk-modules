package com.esb.foonnel.rest.http;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.api.OutboundProperties;

import java.util.Map;

public enum OutboundProperty {

    STATUS("status"),
    HEADERS("headers");

    String name;

    OutboundProperty(String name) {
        this.name = name;
    }

    public int getInt(Message message) {
        return Integer.valueOf(getString(message));
    }

    public String getString(Message message) {
        return (String) get(message);
    }

    public Map<String,String> getMap(Message message) {
        return (Map<String,String>) get(message);
    }

    public Object get(Message message) {
        OutboundProperties outboundProperties = message.getOutboundProperties();
        return outboundProperties.getProperty(name);
    }

    public void set(Message message, Object value) {
        OutboundProperties outboundProperties = message.getOutboundProperties();
        outboundProperties.setProperty(name, value);
    }

}
