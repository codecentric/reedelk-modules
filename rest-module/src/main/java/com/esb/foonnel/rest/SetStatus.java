package com.esb.foonnel.rest;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.api.Processor;
import com.esb.foonnel.rest.http.OutboundProperty;
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
