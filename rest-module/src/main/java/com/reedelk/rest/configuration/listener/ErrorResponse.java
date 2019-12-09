package com.reedelk.rest.configuration.listener;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.script.dynamicmap.DynamicStringMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicInteger;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = ErrorResponse.class, scope = PROTOTYPE)
public class ErrorResponse implements Implementor {

    @Hint("error body text")
    @Default("#[error]")
    @AutoCompleteContributor(error = true, message = false)
    @Property("Response body")
    private DynamicByteArray body;

    @Hint("500")
    @Default("500")
    @AutoCompleteContributor(error = true, message = false)
    @Property("Response status")
    private DynamicInteger status;

    @TabGroup("Headers")
    @AutoCompleteContributor(error = true, message = false)
    @Property("Additional Headers")
    private DynamicStringMap headers = DynamicStringMap.empty();

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

    public DynamicStringMap getHeaders() {
        return headers;
    }

    public void setHeaders(DynamicStringMap headers) {
        this.headers = headers;
    }
}
