package com.reedelk.esb.services.scriptengine.converter;

import com.reedelk.runtime.api.message.type.TypedPublisher;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ValueConverterFactory {

    private static final Map<Class<?>, Map<Class<?>, ValueConverter<?, ?>>> CONVERTERS;

    static {
        Map<Class<?>, Map<Class<?>, ValueConverter<?, ?>>> tmp = new HashMap<>();
        tmp.put(Double.class, com.reedelk.esb.services.scriptengine.converter.doubletype.Converters.ALL);
        tmp.put(String.class, com.reedelk.esb.services.scriptengine.converter.stringtype.Converters.ALL);
        tmp.put(Integer.class, com.reedelk.esb.services.scriptengine.converter.integertype.Converters.ALL);
        tmp.put(Boolean.class, com.reedelk.esb.services.scriptengine.converter.booleantype.Converters.ALL);
        tmp.put(byte[].class, com.reedelk.esb.services.scriptengine.converter.bytearraytype.Converters.ALL);
        tmp.put(Exception.class, com.reedelk.esb.services.scriptengine.converter.exceptiontype.Converters.ALL);
        CONVERTERS = Collections.unmodifiableMap(tmp);
    }

    private ValueConverterFactory() {
    }

    public static <O> O convert(Object input, Class<O> outputClass) {
        return input == null ?
                null :
                convert(input, input.getClass(), outputClass);
    }

    public static <I, O> O convert(Object input, Class<I> inputClass, Class<O> outputClass) {
        Map<Class<?>, ValueConverter<?, ?>> fromConverters = CONVERTERS.get(inputClass);

        if (fromConverters != null) {
            ValueConverter<I, O> toConverters = (ValueConverter<I, O>) fromConverters.get(outputClass);
            if (toConverters != null) return toConverters.from((I) input);

        } else if (input instanceof Exception) {
            Map<Class<?>, ValueConverter<?, ?>> fromExceptionConverters = CONVERTERS.get(Exception.class);
            ValueConverter<I, O> toConverters = (ValueConverter<I, O>) fromExceptionConverters.get(outputClass);
            if (toConverters != null) return toConverters.from((I) input);

        }

        if (Object.class.equals(outputClass)) {
            return (O) input;
        }

        throw new IllegalStateException(String.format("Converter from [%s] to [%s] not available", inputClass, outputClass));
    }

    public static <I, O> Publisher<O> convertTypedPublisher(TypedPublisher<I> input, Class<I> inputClass, Class<O> outputClass) {
        Map<Class<?>, ValueConverter<?, ?>> fromConverters = CONVERTERS.get(inputClass);
        if (fromConverters != null) {
            ValueConverter<I, O> typedPublisherConverter = (ValueConverter<I, O>) fromConverters.get(outputClass);
            if (typedPublisherConverter != null) return typedPublisherConverter.from(input);
        }
        throw new IllegalStateException(String.format("Converter from [%s] to [%s] not available", inputClass, outputClass));
    }
}
