package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.api.resource.ResourceBinary;
import org.reactivestreams.Publisher;

public class ProxyResourceBinary extends ResourceBinary {

    private final Publisher<byte[]> data;

    public ProxyResourceBinary(ResourceBinary original, Publisher<byte[]> data) {
        super(original.path());
        this.data = data;
    }

    @Override
    public Publisher<byte[]> data() {
        return data;
    }
}