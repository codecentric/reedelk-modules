package com.reedelk.esb.services.converter;

import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.converter.Converters;
import com.reedelk.runtime.converter.TypedPublisherConverters;

public class DefaultConverterService implements ConverterService {

    private static final DefaultConverterService INSTANCE = new DefaultConverterService();

    private DefaultConverterService() {
    }

    public static ConverterService getInstance() {
        return INSTANCE;
    }

    @Override
    public <O> O convert(Object input, Class<O> outputClass) {
        return Converters.getInstance().convert(input, outputClass);
    }

    @Override
    public <I, O> TypedPublisher<O> convert(TypedPublisher<I> input, Class<O> outputClass) {
        return TypedPublisherConverters.getInstance().convert(input, outputClass);
    }
}
