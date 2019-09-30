package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.converter.stringtype.AsByteArray;
import com.reedelk.esb.services.scriptengine.converter.stringtype.AsInteger;
import com.reedelk.esb.services.scriptengine.converter.stringtype.AsString;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

class DynamicValueConverterFactory {

    private static final Map<Class<?>, Map<Class<?>, DynamicValueConverter<?,?>>> CONVERTERS;
    static {
        Map<Class<?>, Map<Class<?>, DynamicValueConverter<?,?>>> tmp = new HashMap<>();
        tmp.put(String.class, FromStringConverters.CONVERTERS);
        tmp.put(Double.class, FromDoubleConverters.CONVERTERS);
        tmp.put(Integer.class, FromIntegerConverters.CONVERTERS);
        tmp.put(byte[].class, FromByteArrayConverters.CONVERTERS);
        tmp.put(Exception.class, FromExceptionConverters.CONVERTERS);
        CONVERTERS = tmp;
    }

    @SuppressWarnings("unchecked")
    static <Input,Output> Output convert(Object input, Class<Input> inputClass, Class<Output> outputClass) {
        Map<Class<?>, DynamicValueConverter<?, ?>> typeConverters = CONVERTERS.get(inputClass);

        if (typeConverters != null) {
            DynamicValueConverter<Input, Output> outputConverters =
                    (DynamicValueConverter<Input, Output>) typeConverters.get(outputClass);
            if (outputConverters != null) {
                return outputConverters.from((Input) input);
            }
        }

        if (String.class.equals(outputClass)) {
            return (Output) input.toString();
        }

        if (Object.class.equals(outputClass)) {
            return (Output) input;
        }

        if (input instanceof Exception) {
            Map<Class<?>, DynamicValueConverter<?, ?>> exceptionConverters = CONVERTERS.get(Exception.class);
            DynamicValueConverter<Input, Output> outputConverters =
                    (DynamicValueConverter<Input, Output>) exceptionConverters.get(outputClass);
            if (outputConverters != null) {
                return outputConverters.from((Input) input);
            }
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

    private static class FromStringConverters {
        static final Map<Class<?>, DynamicValueConverter<?, ?>> CONVERTERS;
        static {
            Map<Class<?>, DynamicValueConverter<?, ?>> tmp = new HashMap<>();
            tmp.put(String.class, new AsString());
            tmp.put(Integer.class, new AsInteger());
            tmp.put(byte[].class, new AsByteArray());
            CONVERTERS = tmp;
        }
    }

    private static class FromIntegerConverters {
        static final Map<Class<?>, DynamicValueConverter<?, ?>> CONVERTERS;
        static {
            Map<Class<?>, DynamicValueConverter<?, ?>> tmp = new HashMap<>();
            tmp.put(String.class, new com.reedelk.esb.services.scriptengine.converter.integertype.AsString());
            tmp.put(Integer.class, new com.reedelk.esb.services.scriptengine.converter.integertype.AsInteger());
            CONVERTERS = tmp;
        }
    }

    private static class FromDoubleConverters {
        static final Map<Class<?>, DynamicValueConverter<?, ?>> CONVERTERS;
        static {
            Map<Class<?>, DynamicValueConverter<?, ?>> tmp = new HashMap<>();
            tmp.put(Integer.class, new com.reedelk.esb.services.scriptengine.converter.doubletype.AsInteger());
            CONVERTERS = tmp;
        }
    }

    private static class FromByteArrayConverters {
        static final Map<Class<?>, DynamicValueConverter<?, ?>> CONVERTERS;
        static {
            Map<Class<?>, DynamicValueConverter<?, ?>> tmp = new HashMap<>();
            tmp.put(byte[].class, new com.reedelk.esb.services.scriptengine.converter.bytearraytype.AsByteArray());
            CONVERTERS = tmp;
        }
    }

    private static class FromExceptionConverters {
        static final Map<Class<?>, DynamicValueConverter<?, ?>> CONVERTERS;
        static {
            Map<Class<?>, DynamicValueConverter<?, ?>> tmp = new HashMap<>();
            tmp.put(String.class, new com.reedelk.esb.services.scriptengine.converter.exceptiontype.AsString());
            tmp.put(byte[].class, new com.reedelk.esb.services.scriptengine.converter.exceptiontype.AsByteArray());
            CONVERTERS = tmp;
        }
    }
}
