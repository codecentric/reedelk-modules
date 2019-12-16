package com.reedelk.rest.configuration.client;

import com.reedelk.runtime.api.annotation.Collapsible;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = AdvancedConfiguration.class, scope = PROTOTYPE)
public class AdvancedConfiguration implements Implementor {

    @Property("Response buffer size")
    @Hint("16384") // 16 * 1024
    private Integer responseBufferSize;

    @Property("Request buffer size")
    @Hint("16384") // 16 * 1024
    private Integer requestBufferSize;

    public Integer getResponseBufferSize() {
        return responseBufferSize;
    }

    public void setResponseBufferSize(Integer responseBufferSize) {
        this.responseBufferSize = responseBufferSize;
    }

    public Integer getRequestBufferSize() {
        return requestBufferSize;
    }

    public void setRequestBufferSize(Integer requestBufferSize) {
        this.requestBufferSize = requestBufferSize;
    }
}
