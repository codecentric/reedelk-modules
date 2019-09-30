package com.reedelk.esb.services.scriptengine.converter.exceptiontype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class AsByteArray implements DynamicValueConverter<Exception,byte[]> {

    @Override
    public byte[] from(Exception value) {
        return StackTraceUtils.asByteArray(value);
    }

    @Override
    public Publisher<byte[]> from(Publisher<Exception> stream) {
        return Flux.from(stream).map(this::from);
    }
}
