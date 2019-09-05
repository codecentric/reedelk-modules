package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.TabGroup;
import com.reedelk.runtime.api.annotation.When;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RestListenerErrorResponse.class, scope = PROTOTYPE)
public class RestListenerErrorResponse implements Implementor {

    @Property("Use custom body")
    @Default("false")
    private boolean useBody;

    @Property("Custom Body")
    @Default("payload")
    @When(propertyName = "useBody", propertyValue = "true")
    private String body;

    @Property("Use custom status")
    @Default("false")
    private boolean useStatus;

    @Property("Custom Status")
    @When(propertyName = "useStatus", propertyValue = "true")
    private Integer status;

    @Property("Use custom reason phrase")
    @Default("false")
    private boolean useReasonPhrase;

    @Property("Custom reason phrase")
    @When(propertyName = "useReasonPhrase", propertyValue = "true")
    private String reasonPhrase;

    @TabGroup("Headers")
    @Property("Additional Headers")
    private Map<String,String> headers;

    public boolean isUseBody() {
        return useBody;
    }

    public void setUseBody(boolean useBody) {
        this.useBody = useBody;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isUseStatus() {
        return useStatus;
    }

    public void setUseStatus(boolean useStatus) {
        this.useStatus = useStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean isUseReasonPhrase() {
        return useReasonPhrase;
    }

    public void setUseReasonPhrase(boolean useReasonPhrase) {
        this.useReasonPhrase = useReasonPhrase;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
