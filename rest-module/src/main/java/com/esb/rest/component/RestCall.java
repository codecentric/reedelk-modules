package com.esb.rest.component;

import com.esb.api.annotation.Default;
import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Required;
import com.esb.api.component.ProcessorSync;
import com.esb.api.message.*;
import com.esb.rest.client.Client;
import com.esb.rest.commons.RestMethod;
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
        Client client;
        try {
            client = new Client(requestUrl, method);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        String restRequestResult = client.make();

        Type contentType = new Type(MimeType.APPLICATION_JSON, String.class);

        TypedContent<String> newContent = new MemoryTypedContent<>(restRequestResult, contentType);

        Message message = new Message();

        message.setTypedContent(newContent);

        return message;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }
}
