package com.reedelk.rest.configuration.listener;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.script.DynamicByteArray;
import com.reedelk.runtime.api.script.DynamicInteger;
import com.reedelk.runtime.api.script.DynamicMap;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = Response.class, scope = PROTOTYPE)
public class Response implements Implementor {

    @Default("#[payload]")
    @Hint("content body text")
    @Property("Response body")
    private DynamicByteArray body;

    @Default("200")
    @Hint("201")
    @Property("Response status")
    private DynamicInteger status;

    @TabGroup("Response headers")
    @Property("Response headers")
    private DynamicMap<String> headers = DynamicMap.empty();

    public DynamicByteArray getBody() {
        return body;
    }

    public void setBody(DynamicByteArray body) {
        this.body = body;
    }

    public DynamicInteger getStatus() {
        return status;
    }

    public void setStatus(DynamicInteger status) {
        this.status = status;
    }

    public DynamicMap<String> getHeaders() {
        return headers;
    }

    public void setHeaders(DynamicMap<String> headers) {
        this.headers = headers;
    }
}
