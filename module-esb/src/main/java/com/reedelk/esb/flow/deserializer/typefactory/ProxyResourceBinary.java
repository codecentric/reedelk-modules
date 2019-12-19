package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.api.resource.ResourceBinary;

public class ProxyResourceBinary extends ResourceBinary {

    private final byte[] data;

    public ProxyResourceBinary(ResourceBinary original, byte[] data) {
        super(original.getResourcePath(), original.getContext());
        this.data = data;
    }

    @Override
    public byte[] data() {
        return data;
    }
}