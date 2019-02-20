package com.esb.foonnel.rest;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.api.Processor;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = SetHeader.class, scope = PROTOTYPE)
public class SetHeader implements Processor {

    private String name;
    private String value;

    @Override
    public Message apply(Message message) {
        Map<String, String> responseHttpHeaders = message.getResponseHttpHeaders();
        Map<String, String> newResponseHttpHeaders = new HashMap<>(responseHttpHeaders);
        newResponseHttpHeaders.put(name, value);
        message.setResponseHttpHeaders(newResponseHttpHeaders);
        return message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
