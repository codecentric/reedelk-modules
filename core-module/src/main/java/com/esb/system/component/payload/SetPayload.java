package com.esb.system.component.payload;

import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.component.Processor;
import com.esb.api.message.*;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Payload")
@Component(service = SetPayload.class, scope = PROTOTYPE)
public class SetPayload implements Processor {

    @Property("Message Payload")
    private String payload;

    @Override
    public Message apply(Message message) {
        TypedContent<String> content = new MemoryTypedContent<>(payload, new Type(MimeType.TEXT, String.class));
        message.setTypedContent(content);
        return message;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}

