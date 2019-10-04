package com.reedelk.esb.services.scriptengine.converter.exceptiontype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsString implements ValueConverter<Exception,String> {

    @Override
    public String from(Exception value) {
        return StackTraceUtils.asString(value);
    }

    @Override
    public TypedPublisher<String> from(TypedPublisher<Exception> stream) {
        return TypedPublisher.fromString(Flux.from(stream).map(this::from));
    }
}
