package com.reedelk.esb.services.scriptengine.converter;

import org.reactivestreams.Publisher;

public interface DynamicValueConverter<I, O> {

    O from(I value);

    Publisher<O> from(Publisher<I> stream);
}
