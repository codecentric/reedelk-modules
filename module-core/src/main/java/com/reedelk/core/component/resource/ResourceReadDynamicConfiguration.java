package com.reedelk.core.component.resource;

import com.reedelk.runtime.api.annotation.Collapsible;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = ResourceReadDynamicConfiguration.class, scope = PROTOTYPE)
public class ResourceReadDynamicConfiguration implements Implementor {

    public static final int DEFAULT_READ_BUFFER_SIZE = 65536;

    @Property("Read buffer size")
    @Hint("65536")
    private Integer readBufferSize;

    public Integer getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(Integer readBufferSize) {
        this.readBufferSize = readBufferSize;
    }
}
