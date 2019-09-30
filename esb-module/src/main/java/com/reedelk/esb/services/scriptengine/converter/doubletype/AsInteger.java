package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueConverter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class AsInteger implements DynamicValueConverter<Double,Integer> {

    @Override
    public Integer from(Double value) {
        return value == null ? null : value.intValue();
    }

    @Override
    public Publisher<Integer> from(Publisher<Double> stream) {
        return Flux.from(stream).map(aDouble -> aDouble == null ? null : aDouble.intValue());
    }
}
