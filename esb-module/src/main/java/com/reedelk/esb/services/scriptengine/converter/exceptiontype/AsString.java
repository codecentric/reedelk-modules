package com.reedelk.esb.services.scriptengine.converter.exceptiontype;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueConverter;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class AsString implements DynamicValueConverter<Exception,String> {

    @Override
    public String from(Exception value) {
        return StackTraceUtils.asString(value);
    }

    @Override
    public Publisher<String> from(Publisher<Exception> stream) {
        return Flux.from(stream).map(this::from);
    }
}
