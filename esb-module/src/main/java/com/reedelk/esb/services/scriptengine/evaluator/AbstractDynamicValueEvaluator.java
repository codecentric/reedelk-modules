package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.commons.FunctionName;
import com.reedelk.esb.services.scriptengine.converter.ValueConverterFactory;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateDynamicValueErrorFunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateDynamicValueFunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.reactivestreams.Publisher;

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

    <S> S convert(Object value, Class<?> targetClazz, ValueProvider provider) {
        if (value == null) {
            return provider.empty();

        } else if (value instanceof TypedPublisher<?>) {
            // Value is a typed stream
            TypedPublisher<?> typedPublisher = (TypedPublisher<?>) value;
            Object converted = ValueConverterFactory.convertTypedPublisher(typedPublisher, targetClazz);
            return provider.from(converted);

        } else {
            // Value is NOT a typed stream
            Object converted = ValueConverterFactory.convert(value, value.getClass(), targetClazz);
            return provider.from(converted);
        }
    }

    <T extends ScriptBlock> String functionNameOf(T scriptBlock, FunctionDefinitionBuilder<T> functionDefinitionBuilder) {
        String valueUUID = scriptBlock.uuid();
        String functionName = uuidFunctionNameMap.get(valueUUID);
        if (functionName != null) return functionName;

        synchronized (this) {
            String currentFunctionName = uuidFunctionNameMap.get(valueUUID);
            if (currentFunctionName != null) return currentFunctionName;

            String computedFunctionName = FunctionName.from(valueUUID);
            String functionDefinition = functionDefinitionBuilder.from(computedFunctionName, scriptBlock);

            // pre-compile the function definition.
            scriptEngine.eval(functionDefinition);
            uuidFunctionNameMap.put(valueUUID, computedFunctionName);
            return computedFunctionName;
        }
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
            Publisher<T> converted = convert(message.payload(), targetType, STREAM_PROVIDER);
            return TypedPublisher.from(converted, targetType);
        }
    }
}
