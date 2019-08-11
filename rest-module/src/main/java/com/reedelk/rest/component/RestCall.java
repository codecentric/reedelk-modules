package com.reedelk.rest.component;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Required;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Message;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("REST Call")
@Component(service = RestCall.class, scope = PROTOTYPE)
public class RestCall implements ProcessorSync {

    @Property("Request url")
    @Default("localhost")
    @Required
    private String requestUrl;

    @Property("Method")
    @Default("GET")
    @Required
    private RestMethod method;

    @Override
    public Message apply(Message input) {
        return input;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }
}
