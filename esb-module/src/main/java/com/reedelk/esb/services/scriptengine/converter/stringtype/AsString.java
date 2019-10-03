package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;

public class AsString implements DynamicValueConverter<String,String> {

    @Override
    public String from(String value) {
        return value;
    }

    @Override
    public TypedPublisher<String> from(TypedPublisher<String> stream) {
        return stream;
    }
}
