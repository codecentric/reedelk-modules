package com.esb.foonnel.rest.component;

import com.esb.foonnel.api.component.Processor;
import com.esb.foonnel.api.message.*;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = SetPayload.class, scope = PROTOTYPE)
public class SetPayload implements Processor {

    private String payload;

    @Override
    public Message apply(Message message) {
        TypedContent<String> content = new MemoryTypedContent<>(
                payload,
                new Type(MimeType.TEXT, String.class));
        message.setTypedContent(content);
        return message;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
