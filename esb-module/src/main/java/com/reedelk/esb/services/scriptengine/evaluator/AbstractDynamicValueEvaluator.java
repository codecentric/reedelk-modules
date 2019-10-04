package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.commons.FunctionName;
import com.reedelk.esb.commons.IsSourceAssignableToTarget;
import com.reedelk.esb.services.scriptengine.converter.ValueConverterFactory;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateDynamicValueErrorFunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateDynamicValueFunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;

import java.util.HashMap;
import java.util.Map;

import static com.reedelk.esb.services.scriptengine.evaluator.ValueProviders.STREAM_PROVIDER;

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
        if (valueToConvert == null) {
            return provider.empty();
        } else if (valueToConvert instanceof TypedPublisher<?>) {
            TypedPublisher<?> typedPublisher = (TypedPublisher<?>) valueToConvert;
            return convert(valueToConvert, typedPublisher.getType(), targetClazz, provider);
        } else {
            return convert(valueToConvert, valueToConvert.getClass(), targetClazz, provider);
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

    /**
     * Evaluate the payload without invoking the script engine. This is an optimization
     * since we can get the payload directly from Java without making an expensive call
     * to the script engine.
     */
    <T> TypedPublisher<T> evaluateMessagePayload(Class<T> targetType, Message message) {
        if (message.getContent().isStream()) {
            // We don't resolve the stream, but we still might need to
            // map its content from source type to a target type.
            TypedPublisher<?> stream = message.getContent().stream();
            return convert(stream, targetType, STREAM_PROVIDER);
        } else {
            return TypedPublisher.from(convert(message.payload(), targetType, STREAM_PROVIDER), targetType);
        }
    }

    @SuppressWarnings("unchecked")
    private <S> S convert(Object valueToConvert, Class<?> sourceClass, Class<?> targetClazz, ValueProvider provider) {
        if (valueToConvert instanceof TypedPublisher<?>) {
            // Value is a stream
            Object converted = ValueConverterFactory.convertTypedPublisher((TypedPublisher) valueToConvert, sourceClass, targetClazz);
            return provider.from(converted);

        } else {
            // Value is not a stream
            if (IsSourceAssignableToTarget.from(sourceClass, targetClazz)) {
                return provider.from(valueToConvert);
            } else {
                Object converted = ValueConverterFactory.convert(valueToConvert, sourceClass, targetClazz);
                return provider.from(converted);
            }
        }
    }
}
