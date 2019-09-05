package com.reedelk.rest.component;

import com.reedelk.rest.commons.OutboundProperty;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Status")
@Component(service = SetStatus.class, scope = PROTOTYPE)
public class SetStatus implements ProcessorSync {

    @Property("Response Status")
    @Default("200")
    private int status;

    @Override
    public Message apply(Message input, Context context) {
        OutboundProperty.STATUS.set(input, status);
        return input;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
