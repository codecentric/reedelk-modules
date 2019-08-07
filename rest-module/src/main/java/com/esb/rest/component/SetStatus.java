package com.esb.rest.component;

import com.esb.api.annotation.Default;
import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Required;
import com.esb.api.component.ProcessorSync;
import com.esb.api.message.Message;
import com.esb.rest.commons.OutboundProperty;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Status")
@Component(service = SetStatus.class, scope = PROTOTYPE)
public class SetStatus implements ProcessorSync {

    @Property("Response Status")
    @Default("200")
    @Required
    private int status;

    @Override
    public Message apply(Message message) {
        OutboundProperty.STATUS.set(message, status);
        return message;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
