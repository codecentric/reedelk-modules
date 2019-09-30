package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class AsBoolean implements DynamicValueConverter<String,Boolean> {

    @Override
    public Boolean from(String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public Publisher<Boolean> from(Publisher<String> stream) {
        return Flux.from(stream).map(this::from);
    }
}
