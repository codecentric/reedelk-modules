package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class DynamicValueConverterFactory {

    private static final Map<Class<?>, Map<Class<?>, DynamicValueConverter<?,?>>> CONVERTERS;
    static {
        Map<Class<?>, Map<Class<?>, DynamicValueConverter<?,?>>> tmp = new HashMap<>();
        tmp.put(String.class, com.reedelk.esb.services.scriptengine.converter.stringtype.Converters.ALL);
        tmp.put(Double.class, com.reedelk.esb.services.scriptengine.converter.doubletype.Converters.ALL);
        tmp.put(Integer.class, com.reedelk.esb.services.scriptengine.converter.integertype.Converters.ALL);
        tmp.put(byte[].class, com.reedelk.esb.services.scriptengine.converter.bytearraytype.Converters.ALL);
        tmp.put(Exception.class, com.reedelk.esb.services.scriptengine.converter.exceptiontype.Converters.ALL);
        CONVERTERS = Collections.unmodifiableMap(tmp);
    }

    private DynamicValueConverterFactory() {
    }

    @SuppressWarnings("unchecked")
    static <Input,Output> Output convert(Object input, Class<Input> inputClass, Class<Output> outputClass) {
        Map<Class<?>, DynamicValueConverter<?, ?>> typeConverters = CONVERTERS.get(inputClass);

        if (typeConverters != null) {
            DynamicValueConverter<Input, Output> outputConverters =
                    (DynamicValueConverter<Input, Output>) typeConverters.get(outputClass);
            if (outputConverters != null) return outputConverters.from((Input) input);

        } else if (input instanceof Exception) {
            Map<Class<?>, DynamicValueConverter<?, ?>> exceptionConverters = CONVERTERS.get(Exception.class);
            DynamicValueConverter<Input, Output> outputConverters =
                    (DynamicValueConverter<Input, Output>) exceptionConverters.get(outputClass);
            if (outputConverters != null) return outputConverters.from((Input) input);
        }

        if (String.class.equals(outputClass)) {
            return (Output) input.toString();
        } else if (Object.class.equals(outputClass)) {
            return (Output) input;
        }

        throw new IllegalStateException(String.format("Converter from [%s] to [%s] not available", inputClass, outputClass));
    }

    @SuppressWarnings("unchecked")
    static <Input,Output> Publisher<Output> convertStream(Publisher<Input> input, Class<Input> inputClass, Class<Output> outputClass) {
        Map<Class<?>, DynamicValueConverter<?, ?>> typeConverters = CONVERTERS.get(inputClass);
        if (typeConverters != null) {
            DynamicValueConverter<Input, Output> outputConverters =
                    (DynamicValueConverter<Input, Output>) typeConverters.get(outputClass);
            if (outputConverters != null) {
                return outputConverters.from(input);
            }
        }
        throw new IllegalStateException(String.format("Converter from [%s] to [%s] not available", inputClass, outputClass));
    }
}
