package com.esb.rest.component;

import com.esb.api.component.Processor;
import com.esb.api.message.Message;
import com.esb.rest.commons.OutboundProperty;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = SetStatus.class, scope = PROTOTYPE)
public class SetStatus implements Processor {

    private int status;

    @Override
    public Message apply(Message message) {
        OutboundProperty.STATUS.set(message, status);
        return message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
