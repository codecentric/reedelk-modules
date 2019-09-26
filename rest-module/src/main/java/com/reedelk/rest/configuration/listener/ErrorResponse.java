package com.reedelk.rest.configuration.listener;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.script.DynamicByteArray;
import com.reedelk.runtime.api.script.DynamicInteger;
import com.reedelk.runtime.api.script.DynamicMap;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = ErrorResponse.class, scope = PROTOTYPE)
public class ErrorResponse implements Implementor {

    @Hint("error body text")
    @Default("#[error]")
    @Property("Response body")
    private DynamicByteArray body;

    @Hint("500")
    @Default("500")
    @Property("Response status")
    private DynamicInteger status;

    @TabGroup("Headers")
    @Property("Additional Headers")
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
