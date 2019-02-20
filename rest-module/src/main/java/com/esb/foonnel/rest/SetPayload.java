package com.esb.foonnel.rest;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.api.Processor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = SetPayload.class, scope = PROTOTYPE)
public class SetPayload implements Processor {

    private String payload;

    @Override
    public Message apply(Message message) {
        message.setContent(payload);
        return message;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
