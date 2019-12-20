package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.api.resource.ResourceText;
import org.reactivestreams.Publisher;

public class ProxyResourceText extends ResourceText {

    private final Publisher<String> data;

    public ProxyResourceText(ResourceText original, Publisher<String> data) {
        super(original.getResourcePath(), original.getContext());
        this.data = data;
    }

    @Override
    public Publisher<String> data() {
        return data;
    }
}