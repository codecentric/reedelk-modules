package com.reedelk.esb.services.converter;

import com.reedelk.runtime.api.message.content.utils.TypedPublisher;

public interface ValueConverter<I, O> {

    O from(I value);

    TypedPublisher<O> from(TypedPublisher<I> stream);
}
