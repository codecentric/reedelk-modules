package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
class DynamicValueConverterFactory {

    private static final Map<Class<?>, Map<Class<?>, DynamicValueConverter<?, ?>>> CONVERTERS;

    static {
        Map<Class<?>, Map<Class<?>, DynamicValueConverter<?, ?>>> tmp = new HashMap<>();
        tmp.put(Double.class, com.reedelk.esb.services.scriptengine.converter.doubletype.Converters.ALL);
        tmp.put(String.class, com.reedelk.esb.services.scriptengine.converter.stringtype.Converters.ALL);
        tmp.put(Integer.class, com.reedelk.esb.services.scriptengine.converter.integertype.Converters.ALL);
        tmp.put(byte[].class, com.reedelk.esb.services.scriptengine.converter.bytearraytype.Converters.ALL);
        tmp.put(Exception.class, com.reedelk.esb.services.scriptengine.converter.exceptiontype.Converters.ALL);
        CONVERTERS = Collections.unmodifiableMap(tmp);
    }

    private DynamicValueConverterFactory() {
    }

    static <O> O convert(Object input, Class<O> outputClass) {
        return input == null ? null : convert(input, input.getClass(), outputClass);
    }

    // TODO: This function is ugly!
    static <I, O> O convert(Object input, Class<I> inputClass, Class<O> outputClass) {
        Map<Class<?>, DynamicValueConverter<?, ?>> fromConverters = CONVERTERS.get(inputClass);

        if (fromConverters != null) {
            DynamicValueConverter<I, O> toConverters =
                    (DynamicValueConverter<I, O>) fromConverters.get(outputClass);
            if (toConverters != null) return toConverters.from((I) input);

        } else if (input instanceof Exception) {
            Map<Class<?>, DynamicValueConverter<?, ?>> fromExceptionConverters = CONVERTERS.get(Exception.class);
            DynamicValueConverter<I, O> toConverters =
                    (DynamicValueConverter<I, O>) fromExceptionConverters.get(outputClass);
            if (toConverters != null) return toConverters.from((I) input);

        }

        if (String.class.equals(outputClass)) {
            return (O) input.toString();
        } else if (Object.class.equals(outputClass)) {
            return (O) input;
        }

        throw new IllegalStateException(String.format("Converter from [%s] to [%s] not available", inputClass, outputClass));
    }

    static <I, O> Publisher<O> convertStream(TypedPublisher<I> input, Class<I> inputClass, Class<O> outputClass) {
        Map<Class<?>, DynamicValueConverter<?, ?>> fromConverters = CONVERTERS.get(inputClass);
        if (fromConverters != null) {
            DynamicValueConverter<I, O> typedPublisherConverter = (DynamicValueConverter<I, O>) fromConverters.get(outputClass);
            if (typedPublisherConverter != null) return typedPublisherConverter.from(input);
        }
        throw new IllegalStateException(String.format("Converter from [%s] to [%s] not available", inputClass, outputClass));
    }
}
