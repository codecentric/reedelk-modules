package com.reedelk.esb.services.scriptengine.converter.exceptiontype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsString implements DynamicValueConverter<Exception,String> {

    @Override
    public String from(Exception value) {
        return StackTraceUtils.asString(value);
    }

    @Override
    public TypedPublisher<String> from(TypedPublisher<Exception> stream) {
        return TypedPublisher.from(Flux.from(stream).map(this::from), String.class);
    }
}
