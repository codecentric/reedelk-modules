package com.reedelk.rest.commons;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;

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
        MessageAttributes attributes = message.getAttributes();
        return attributes.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    public Map<String,String> getMap(Message message) {
        return (Map<String,String>) get(message);
    }

    public Object get(Message message) {
        MessageAttributes attributes = message.getAttributes();
        return attributes.get(name);
    }

    public void set(Message message, Serializable value) {
        MessageAttributes attributes = message.getAttributes();
        attributes.put(name, value);
    }
}
