package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = RestListenerResponse.class, scope = PROTOTYPE)
public class RestListenerResponse implements Implementor {

    @Script
    @Property("Body")
    @Default("payload")
    private String body;

    @Script
    @Property("Status")
    @Default("'200'")
    private String status;

    @TabGroup("Headers")
    @Property("Additional Headers")
    private Map<String,String> headers = Collections.emptyMap();

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
