package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.api.resource.ResourceText;

public class ProxyResourceText extends ResourceText {

    private final String data;

    public ProxyResourceText(ResourceText original, String data) {
        super(original.getResourcePath(), original.getContext());
        this.data = data;
    }

    @Override
    public String data() {
        return data;
    }
}