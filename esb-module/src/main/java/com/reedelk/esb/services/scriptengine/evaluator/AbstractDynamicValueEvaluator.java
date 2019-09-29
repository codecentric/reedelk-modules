package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateErrorFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionBuilder;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.DynamicValue;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractDynamicValueEvaluator extends ScriptEngineServiceAdapter {

    private final Map<String, String> ORIGIN_FUNCTION_NAME = new HashMap<>();

    static final FunctionBuilder ERROR_FUNCTION = new EvaluateErrorFunctionBuilder();
    static final FunctionBuilder FUNCTION = new EvaluateFunctionBuilder();


    private final ScriptEngine engine;
    private final Invocable invocable;

    AbstractDynamicValueEvaluator(ScriptEngine engine, Invocable invocable) {
        this.engine = engine;
        this.invocable = invocable;
    }

    <ConvertedType, T> ConvertedType execute(DynamicValue<T> dynamicValue, ValueProvider<ConvertedType> provider, FunctionBuilder functionBuilder, Object... args) {
        if (dynamicValue.isEmptyScript()) {
            return provider.empty();
        }
        String functionName = functionNameOf(dynamicValue, functionBuilder);
        try {
            Object evaluationResult = invocable.invokeFunction(functionName, args);
            return convert(evaluationResult, dynamicValue.getEvaluatedType(), provider);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new ESBException(e);
        }
    }

    <ConvertedType> ConvertedType convert(Object valueToConvert, Class<?> targetClazz, ValueProvider<ConvertedType> provider) {
        return convert(valueToConvert, valueToConvert.getClass(), targetClazz, provider);
    }

    <ConvertedType> ConvertedType convert(Object valueToConvert, Class<?> sourceClass, Class<?> targetClazz, ValueProvider<ConvertedType> provider) {
        if (valueToConvert == null) {
            return provider.empty();
        } else if (sourceAssignableToTarget(sourceClass, targetClazz)) {
            return provider.from(valueToConvert);
        } else {
            Object converted = DynamicValueConverterFactory.convert(valueToConvert, valueToConvert.getClass(), targetClazz);
            return provider.from(converted);
        }
    }

    interface ValueProvider<ConvertedType> {
        ConvertedType empty();
        ConvertedType from(Object value);
    }

    private <T> String functionNameOf(DynamicValue<T> dynamicValue, FunctionBuilder functionBuilder) {
        String valueUUID =  dynamicValue.getUUID();
        String functionName = ORIGIN_FUNCTION_NAME.get(valueUUID);
        if (functionName == null) {
            synchronized (this) {
                if (!ORIGIN_FUNCTION_NAME.containsKey(valueUUID)) {
                    functionName = "fun" + valueUUID;
                    String scriptBody = dynamicValue.getBody();
                    String functionDefinition =
                            functionBuilder.build(functionName, ScriptUtils.unwrap(scriptBody));
                    // Compiling the function definition.
                    try {
                        engine.eval(functionDefinition);
                        ORIGIN_FUNCTION_NAME.put(valueUUID, functionName);
                    } catch (ScriptException e) {
                        throw new ESBException(e);
                    }
                }
            }
        }
        return functionName;
    }

    private static boolean sourceAssignableToTarget(Class<?> sourceClazz, Class<?> targetClazz) {
        return sourceClazz.isAssignableFrom(targetClazz);
    }
}
