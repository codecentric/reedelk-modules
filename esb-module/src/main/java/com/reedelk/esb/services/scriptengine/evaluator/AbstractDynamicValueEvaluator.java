package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateErrorFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionBuilder;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
abstract class AbstractDynamicValueEvaluator extends ScriptEngineServiceAdapter {

    private static final String FUNCTION_NAME_TEMPLATE = "fun_%s";

    static final FunctionBuilder ERROR_FUNCTION = new EvaluateErrorFunctionBuilder();
    static final FunctionBuilder FUNCTION = new EvaluateFunctionBuilder();

    final Map<String, String> uuidFunctionNameMap = new HashMap<>();
    final ScriptEngineProvider scriptEngine;

    AbstractDynamicValueEvaluator(ScriptEngineProvider scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    <S, T> S execute(DynamicValue<T> dynamicValue, ValueProvider provider, FunctionBuilder functionBuilder, Object... args) {
        if (dynamicValue.isEmptyScript()) {
            return provider.empty();
        } else {
            String functionName = functionNameOf(dynamicValue, functionBuilder);
            Object evaluationResult = scriptEngine.invokeFunction(functionName, args);
            return convert(evaluationResult, dynamicValue.getEvaluatedType(), provider);
        }
    }

    <S> S convert(Object valueToConvert, Class<?> targetClazz, ValueProvider provider) {
        return valueToConvert == null ?
                provider.empty() :
                convert(valueToConvert, valueToConvert.getClass(), targetClazz, provider);
    }

    <S> S convert(Object valueToConvert, Class<?> sourceClass, Class<?> targetClazz, ValueProvider provider) {
        if (valueToConvert instanceof Publisher<?>) {
            // Value is a stream
            Object converted = DynamicValueConverterFactory.convertStream((Publisher) valueToConvert, sourceClass, targetClazz);
            return provider.from(converted);

        } else {
            // Value is not a stream
            if (sourceAssignableToTarget(sourceClass, targetClazz)) {
                return provider.from(valueToConvert);
            } else {
                Object converted = DynamicValueConverterFactory.convert(valueToConvert, sourceClass, targetClazz);
                return provider.from(converted);
            }
        }
    }

    interface ValueProvider {
        <S> S empty();
        <S> S from(Object value);
    }

    String functionNameOf(ScriptBlock scriptBlock, FunctionBuilder functionBuilder) {
        String valueUUID = scriptBlock.getUUID();
        String functionName = uuidFunctionNameMap.get(valueUUID);
        if (functionName == null) {
            synchronized (this) {
                if (!uuidFunctionNameMap.containsKey(valueUUID)) {
                    functionName = functionNameFrom(valueUUID);
                    String scriptBody = scriptBlock.getScriptBody();
                    String functionDefinition = functionBuilder.build(functionName, ScriptUtils.unwrap(scriptBody));

                    // pre-compile the function definition.
                    scriptEngine.eval(functionDefinition);
                    uuidFunctionNameMap.put(valueUUID, functionName);
                }
            }
        }
        return functionName;
    }

    static String functionNameFrom(String uuid) {
        return String.format(FUNCTION_NAME_TEMPLATE, uuid);
    }

    static final ValueProvider OPTIONAL_PROVIDER = new OptionalValueProvider();

    static final ValueProvider STREAM_PROVIDER = new StreamValueProvider();

    private static class OptionalValueProvider implements ValueProvider {
        @Override
        public Optional<?> empty() {
            return Optional.empty();
        }

        @Override
        public Optional<?> from(Object value) {
            return Optional.ofNullable(value);
        }
    }

    private static class StreamValueProvider implements ValueProvider {
        @Override
        public Publisher<?> empty() {
            return Mono.empty();
        }

        @Override
        public Publisher<?> from(Object value) {
            if (value == null) {
                return Mono.empty();
            } else if (value instanceof Publisher<?>) {
                return (Publisher<?>) value;
            } else {
                return Mono.just(value);
            }
        }
    }

    private static boolean sourceAssignableToTarget(Class<?> sourceClazz, Class<?> targetClazz) {
        return sourceClazz.isAssignableFrom(targetClazz);
    }
}
