package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueConverter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class AsString implements DynamicValueConverter<Integer,String> {

    @Override
    public String from(Integer value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Publisher<String> from(Publisher<Integer> stream) {
        return Flux.from(stream).map(String::valueOf);
    }
}
