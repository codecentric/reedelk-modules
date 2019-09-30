package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateErrorFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionBuilder;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.DynamicValue;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractDynamicValueEvaluator extends ScriptEngineServiceAdapter {

    private static final String FUNCTION_NAME_TEMPLATE = "fun_%s";

    static final FunctionBuilder ERROR_FUNCTION = new EvaluateErrorFunctionBuilder();
    static final FunctionBuilder FUNCTION = new EvaluateFunctionBuilder();

    final Map<String, String> uuidFunctionNameMap = new HashMap<>();
    final ScriptEngineProvider scriptEngine;

    AbstractDynamicValueEvaluator(ScriptEngineProvider scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    <ConvertedType, T> ConvertedType execute(DynamicValue<T> dynamicValue, ValueProvider<ConvertedType> provider, FunctionBuilder functionBuilder, Object... args) {
        if (dynamicValue.isEmptyScript()) {
            return provider.empty();
        } else {
            String functionName = functionNameOf(dynamicValue, functionBuilder);
            Object evaluationResult = scriptEngine.invokeFunction(functionName, args);
            return convert(evaluationResult, dynamicValue.getEvaluatedType(), provider);
        }
    }

    <ConvertedType> ConvertedType convert(Object valueToConvert, Class<?> targetClazz, ValueProvider<ConvertedType> provider) {
        return valueToConvert == null ?
                provider.empty() :
                convert(valueToConvert, valueToConvert.getClass(), targetClazz, provider);
    }

    <ConvertedType> ConvertedType convert(Object valueToConvert, Class<?> sourceClass, Class<?> targetClazz, ValueProvider<ConvertedType> provider) {
        // Value is a stream
        if (valueToConvert instanceof Publisher<?>) {
            Object converted = DynamicValueConverterFactory.convertStream((Publisher) valueToConvert, sourceClass, targetClazz);
            return provider.from(converted);

            // Value is not a stream
        } else {
            if (sourceAssignableToTarget(sourceClass, targetClazz)) {
                return provider.from(valueToConvert);
            } else {
                Object converted = DynamicValueConverterFactory.convert(valueToConvert, sourceClass, targetClazz);
                return provider.from(converted);
            }
        }
    }

    interface ValueProvider<ConvertedType> {
        ConvertedType empty();

        ConvertedType from(Object value);
    }

    private <T> String functionNameOf(DynamicValue<T> dynamicValue, FunctionBuilder functionBuilder) {
        String valueUUID = dynamicValue.getUUID();
        String functionName = uuidFunctionNameMap.get(valueUUID);
        if (functionName == null) {
            synchronized (this) {
                if (!uuidFunctionNameMap.containsKey(valueUUID)) {
                    functionName = functionNameFrom(valueUUID);
                    String scriptBody = dynamicValue.getBody();
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

    private static boolean sourceAssignableToTarget(Class<?> sourceClazz, Class<?> targetClazz) {
        return sourceClazz.isAssignableFrom(targetClazz);
    }
}
