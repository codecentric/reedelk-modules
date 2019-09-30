package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueConverter;
import org.reactivestreams.Publisher;

public class AsString implements DynamicValueConverter<String,String> {

    @Override
    public String from(String value) {
        return value;
    }

    @Override
    public Publisher<String> from(Publisher<String> stream) {
        return stream;
    }
}
