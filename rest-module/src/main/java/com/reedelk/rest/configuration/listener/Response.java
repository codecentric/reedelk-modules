package com.reedelk.rest.configuration.listener;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = Response.class, scope = PROTOTYPE)
public class Response implements Implementor {

    @ScriptInline
    @Default("#[payload]")
    @Hint("content body text")
    @Property("Response body")
    private String body;

    @ScriptInline
    @Default("200")
    @Hint("201")
    @Property("Response status")
    private String status;

    @TabGroup("Response headers")
    @Property("Response headers")
    private Map<String, String> headers = Collections.emptyMap();

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
