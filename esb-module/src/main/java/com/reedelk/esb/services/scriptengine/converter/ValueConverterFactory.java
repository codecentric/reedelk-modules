package com.reedelk.esb.services.scriptengine.converter;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.type.TypedPublisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;

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
        return input == null ? null : convert(input, input.getClass(), outputClass);
    }

    public static <I, O> O convert(Object input, Class<I> inputClass, Class<O> outputClass) {
        if (input == null) {
            return null;
        } else if (inputClass.equals(outputClass)) {
            return (O) input;
        } else if (Object.class.equals(outputClass)) {
            return (O) input;
        } else if (input instanceof Exception) {
            return convertType(input, Exception.class, outputClass);
        } else {
            return convertType(input, inputClass, outputClass);
        }
    }

    public static <I, O> TypedPublisher<O> convertTypedPublisher(TypedPublisher<I> input, Class<O> outputClass) {
        if (input == null) {
            return null;
        } else if (input.getType().equals(outputClass)) {
            return (TypedPublisher<O>) input;
        } else {
            return Optional.ofNullable(CONVERTERS.get(input.getType()))
                    .flatMap(fromConverter -> Optional.ofNullable((ValueConverter<I, O>) fromConverter.get(outputClass)))
                    .map(toConverter -> toConverter.from(input))
                    .orElseThrow(converterNotFound(input.getType(), outputClass));
        }
    }

    private static <I, O> O convertType(I input, Class<?> inputClass, Class<O> outputClass) {
        return Optional.ofNullable(CONVERTERS.get(inputClass))
                .flatMap(fromConverter -> Optional.ofNullable((ValueConverter<I, O>) fromConverter.get(outputClass)))
                .map(toConverter -> toConverter.from(input))
                .orElseThrow(converterNotFound(inputClass, outputClass));
    }

    private static Supplier<? extends ESBException> converterNotFound(Class<?> inputClazz, Class<?> outputClazz) {
        return () -> new ESBException(format("Converter for input=[%s] to output=[%s] not available", inputClazz.getName(), outputClazz.getName()));
    }
}
