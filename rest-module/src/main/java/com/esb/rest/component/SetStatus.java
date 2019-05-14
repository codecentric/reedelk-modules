package com.esb.rest.component;

import com.esb.api.annotation.DefaultValue;
import com.esb.api.annotation.DisplayName;
import com.esb.api.annotation.EsbComponent;
import com.esb.api.annotation.Required;
import com.esb.api.component.Processor;
import com.esb.api.message.Message;
import com.esb.rest.commons.OutboundProperty;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@EsbComponent
@Component(service = SetStatus.class, scope = PROTOTYPE)
public class SetStatus implements Processor {

    @Required
    @DefaultValue(intValue = 200)
    @DisplayName("Response Status")
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
