package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RestListenerErrorResponse.class, scope = PROTOTYPE)
public class RestListenerErrorResponse implements Implementor {

    @Property("Use custom body")
    private Boolean useBody;

    @Script
    @Property("Custom Body")
    @Default("payload")
    @When(propertyName = "useBody", propertyValue = "true")
    private String body;

    @Property("Use custom status")
    private Boolean useStatus;

    @Property("Custom Status")
    @When(propertyName = "useStatus", propertyValue = "true")
    private Integer status;

    @TabGroup("Headers")
    @Property("Additional Headers")
    private Map<String,String> headers = Collections.emptyMap();

    public Boolean getUseBody() {
        return useBody;
    }

    public void setUseBody(Boolean useBody) {
        this.useBody = useBody;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(Boolean useStatus) {
        this.useStatus = useStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
