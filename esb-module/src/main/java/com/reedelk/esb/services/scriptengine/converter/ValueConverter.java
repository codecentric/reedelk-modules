package com.reedelk.esb.services.scriptengine.converter;

import com.reedelk.runtime.api.message.type.TypedPublisher;

public interface ValueConverter<I, O> {

    O from(I value);

    TypedPublisher<O> from(TypedPublisher<I> stream);
}
