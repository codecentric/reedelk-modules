package com.reedelk.core.component.payload;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Payload")
@Component(service = SetPayload.class, scope = PROTOTYPE)
public class SetPayload implements ProcessorSync {

    @Property("Message Payload")
    private String payload;

    @Override
    public Message apply(Message input, Context context) {
        return MessageBuilder.get()
                .text(payload)
                .build();
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}

