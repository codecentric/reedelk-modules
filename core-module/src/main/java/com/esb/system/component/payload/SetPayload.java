package com.esb.system.component.payload;

import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.component.ProcessorSync;
import com.esb.api.message.Message;
import com.esb.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Payload")
@Component(service = SetPayload.class, scope = PROTOTYPE)
public class SetPayload implements ProcessorSync {

    @Property("Message Payload")
    private String payload;

    @Override
    public Message apply(Message message) {
        return MessageBuilder.get()
                .text(payload)
                .build();
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}

