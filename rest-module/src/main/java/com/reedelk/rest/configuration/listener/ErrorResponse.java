package com.reedelk.rest.configuration.listener;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicValue;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = ErrorResponse.class, scope = PROTOTYPE)
public class ErrorResponse implements Implementor {

    @Hint("error body text")
    @Default("#[error]")
    @Property("Response body")
    private DynamicValue body;

    @Hint("500")
    @Default("500")
    @Property("Response status")
    private DynamicValue status;

    @TabGroup("Headers")
    @Property("Additional Headers")
    private DynamicMap<String> headers = DynamicMap.empty();

    public DynamicValue getBody() {
        return body;
    }

    public void setBody(DynamicValue body) {
        this.body = body;
    }

    public DynamicValue getStatus() {
        return status;
    }

    public void setStatus(DynamicValue status) {
        this.status = status;
    }

    public DynamicMap<String> getHeaders() {
        return headers;
    }

    public void setHeaders(DynamicMap<String> headers) {
        this.headers = headers;
    }
}
