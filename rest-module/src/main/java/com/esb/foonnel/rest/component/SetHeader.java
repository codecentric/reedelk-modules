package com.esb.foonnel.rest.component;

import com.esb.foonnel.api.component.Processor;
import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.rest.commons.OutboundProperty;
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
        Map<String, String> outboundHeaders = OutboundProperty.HEADERS.getMap(message);
        if (outboundHeaders == null) {
            outboundHeaders = new HashMap<>();
        }
        outboundHeaders.put(name, value);
        OutboundProperty.HEADERS.set(message, outboundHeaders);
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
