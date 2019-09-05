package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Script;
import com.reedelk.runtime.api.annotation.TabGroup;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RestListenerErrorResponse.class, scope = PROTOTYPE)
public class RestListenerErrorResponse implements Implementor {

    @Script
    @Property("Body")
    @Default("payload")
    private String body;

    @Property("Status")
    private Integer status;

    @Property("Reason phrase")
    private String reasonPhrase;

    @TabGroup("Headers")
    @Property("Headers")
    private Map<String,String> headers;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }
}
