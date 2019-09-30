package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueConverter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class AsInteger implements DynamicValueConverter<String,Integer> {

    @Override
    public Integer from(String value) {
        return Integer.valueOf(value);
    }

    @Override
    public Publisher<Integer> from(Publisher<String> stream) {
        return Flux.from(stream).map(Integer::valueOf);
    }
}
