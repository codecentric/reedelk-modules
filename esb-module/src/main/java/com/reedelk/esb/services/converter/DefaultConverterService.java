package com.reedelk.esb.services.converter;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.service.ConverterService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;

@SuppressWarnings("unchecked")
public class DefaultConverterService implements ConverterService {

    private static final DefaultConverterService INSTANCE = new DefaultConverterService();

    private static class DefaultConverterServiceHelper {
        private static final Map<Class<?>, ValueConverter<?,?>> DEFAULT =
                Collections.unmodifiableMap(com.reedelk.esb.services.converter.defaulttype.Converters.ALL);

        private static final Map<Class<?>, Map<Class<?>, ValueConverter<?, ?>>> CONVERTERS;
        static {
            Map<Class<?>, Map<Class<?>, ValueConverter<?, ?>>> tmp = new HashMap<>();
            tmp.put(Float.class, com.reedelk.esb.services.converter.floattype.Converters.ALL);
            tmp.put(Double.class, com.reedelk.esb.services.converter.doubletype.Converters.ALL);
            tmp.put(String.class, com.reedelk.esb.services.converter.stringtype.Converters.ALL);
            tmp.put(Integer.class, com.reedelk.esb.services.converter.integertype.Converters.ALL);
            tmp.put(Boolean.class, com.reedelk.esb.services.converter.booleantype.Converters.ALL);
            tmp.put(byte[].class, com.reedelk.esb.services.converter.bytearraytype.Converters.ALL);
            tmp.put(Byte[].class, com.reedelk.esb.services.converter.bytearraytype.Converters.ALL);
            tmp.put(Exception.class, com.reedelk.esb.services.converter.exceptiontype.Converters.ALL);
            CONVERTERS = Collections.unmodifiableMap(tmp);
        }
    }

    private DefaultConverterService() {
    }

    public static DefaultConverterService getInstance() {
        return INSTANCE;
    }

    private Map<Class<?>, Map<Class<?>, ValueConverter<?, ?>>> converters() {
        return DefaultConverterServiceHelper.CONVERTERS;
    }

    private Map<Class<?>, ValueConverter<?,?>> defaults() {
        return DefaultConverterServiceHelper.DEFAULT;
    }

    @Override
    public <O> O convert(Object input, Class<O> outputClass) {
        if (input == null) {
            return null;
        } else {
            return convert(input, input.getClass(), outputClass);
        }
    }

    @Override
    public <I, O> TypedPublisher<O> convert(TypedPublisher<I> input, Class<O> outputClass) {
        if (input == null) {
            return null;
        } else if (input.getType().equals(outputClass)) {
            return (TypedPublisher<O>) input;
        } else if  (Object.class.equals(outputClass)) {
            return (TypedPublisher<O>) input;
        } else {
            return Optional.ofNullable(converters().get(input.getType()))
                    .flatMap(fromConverter -> Optional.ofNullable((ValueConverter<I, O>) fromConverter.get(outputClass)))
                    .map(toConverter -> toConverter.from(input))
                    .orElseThrow(converterNotFound(input.getType(), outputClass));
        }
    }

    private <I, O> O convert(Object input, Class<I> inputClass, Class<O> outputClass) {
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

    private <I, O> O convertType(I input, Class<?> inputClass, Class<O> outputClass) {
        return Optional.ofNullable(converters().getOrDefault(inputClass, defaults()))
                .flatMap(fromConverter -> Optional.ofNullable((ValueConverter<I, O>) fromConverter.get(outputClass)))
                .map(toConverter -> toConverter.from(input))
                .orElseThrow(converterNotFound(inputClass, outputClass));
    }

    private Supplier<? extends ESBException> converterNotFound(Class<?> inputClazz, Class<?> outputClazz) {
        return () -> new ESBException(format("Converter for input=[%s] to output=[%s] not available", inputClazz.getName(), outputClazz.getName()));
    }
}
