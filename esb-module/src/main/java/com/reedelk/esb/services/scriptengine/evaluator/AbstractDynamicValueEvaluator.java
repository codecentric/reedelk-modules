package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.commons.FunctionName;
import com.reedelk.esb.commons.IsSourceAssignableToTarget;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateDynamicValueErrorFunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateDynamicValueFunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractDynamicValueEvaluator extends ScriptEngineServiceAdapter {

    static final FunctionDefinitionBuilder<DynamicValue> ERROR_FUNCTION = new EvaluateDynamicValueErrorFunctionDefinitionBuilder();
    static final FunctionDefinitionBuilder<DynamicValue> FUNCTION = new EvaluateDynamicValueFunctionDefinitionBuilder();

    final ScriptEngineProvider scriptEngine;

    private final Map<String, String> uuidFunctionNameMap = new HashMap<>();

    AbstractDynamicValueEvaluator(ScriptEngineProvider scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    <S, T> S execute(DynamicValue<T> dynamicValue, ValueProvider provider, FunctionDefinitionBuilder<DynamicValue> functionDefinitionBuilder, Object... args) {
        if (dynamicValue.isEmpty()) {
            return provider.empty();
        } else {
            String functionName = functionNameOf(dynamicValue, functionDefinitionBuilder);
            Object evaluationResult = scriptEngine.invokeFunction(functionName, args);
            return convert(evaluationResult, dynamicValue.getEvaluatedType(), provider);
        }
    }

    <S> S convert(Object valueToConvert, Class<?> targetClazz, ValueProvider provider) {
        return valueToConvert == null ?
                provider.empty() :
                convert(valueToConvert, valueToConvert.getClass(), targetClazz, provider);
    }

    @SuppressWarnings("unchecked")
    <S> S convert(Object valueToConvert, Class<?> sourceClass, Class<?> targetClazz, ValueProvider provider) {
        if (valueToConvert instanceof Publisher<?>) {
            // Value is a stream
            Object converted = DynamicValueConverterFactory.convertStream((Publisher) valueToConvert, sourceClass, targetClazz);
            return provider.from(converted);

        } else {
            // Value is not a stream
            if (IsSourceAssignableToTarget.from(sourceClass, targetClazz)) {
                return provider.from(valueToConvert);
            } else {
                Object converted = DynamicValueConverterFactory.convert(valueToConvert, sourceClass, targetClazz);
                return provider.from(converted);
            }
        }
    }

    <T extends ScriptBlock> String functionNameOf(T scriptBlock, FunctionDefinitionBuilder<T> functionDefinitionBuilder) {
        String valueUUID = scriptBlock.uuid();
        String functionName = uuidFunctionNameMap.get(valueUUID);
        if (functionName == null) {
            synchronized (this) {
                if (!uuidFunctionNameMap.containsKey(valueUUID)) {
                    functionName = FunctionName.from(valueUUID);
                    String functionDefinition = functionDefinitionBuilder.from(functionName, scriptBlock);

                    // pre-compile the function definition.
                    scriptEngine.eval(functionDefinition);
                    uuidFunctionNameMap.put(valueUUID, functionName);
                }
            }
        }
        return functionName;
    }
}
