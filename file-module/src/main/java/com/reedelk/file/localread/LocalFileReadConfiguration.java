package com.reedelk.file.localread;

import com.reedelk.runtime.api.annotation.Collapsible;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = LocalFileReadConfiguration.class, scope = PROTOTYPE)
public class LocalFileReadConfiguration implements Implementor {

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
