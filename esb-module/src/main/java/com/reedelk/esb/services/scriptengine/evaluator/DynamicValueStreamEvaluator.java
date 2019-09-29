package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicValue;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class DynamicValueStreamEvaluator extends AbstractDynamicValueEvaluator {

    public DynamicValueStreamEvaluator(ScriptEngine engine, Invocable invocable) {
        super(engine, invocable);
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        if (dynamicValue == null) {
            return Mono.empty();
        } else if (dynamicValue.isScript()) {
            if (dynamicValue.isEvaluateMessagePayload()) {
                // We avoid evaluating the payload (optimization)
                return convert(message.payload(), dynamicValue.getEvaluatedType());
            } else {
                return execute(dynamicValue, INLINE_SCRIPT, message, flowContext);
            }
        } else {
            // Not a script
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getBody(), String.class, dynamicValue.getEvaluatedType());
            return Mono.just(converted);
        }
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Throwable throwable, FlowContext flowContext) {
        if (dynamicValue == null) {
            return Mono.empty();
        } else if (dynamicValue.isScript()) {
            // Script
            return execute(dynamicValue, INLINE_ERROR_SCRIPT, throwable, flowContext);
        } else {
            // Not a script
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getBody(), String.class, dynamicValue.getEvaluatedType());
            return Mono.just(converted);
        }
    }

    private <T> Publisher<T> execute(DynamicValue<T> dynamicValue, String template, Object... args) {
        // If script is empty, no need to evaluate it.
        if (dynamicValue.isEmptyScript()) {
            return Mono.empty();
        }

        String functionName = functionNameOf(dynamicValue, template);
        try {
            Object evaluationResult = invocable.invokeFunction(functionName, args);
            return convert(evaluationResult, dynamicValue.getEvaluatedType());
        } catch (ScriptException | NoSuchMethodException e) {
            throw new ESBException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Publisher<T> convert(Object valueToConvert, Class<?> targetClazz) {
        if (valueToConvert == null) {
            return Mono.empty();
        } else if (sourceAssignableToTarget(valueToConvert.getClass(), targetClazz)) {
            return Mono.just((T) valueToConvert);
        } else {
            T converted = (T) DynamicValueConverterFactory.convert(valueToConvert, valueToConvert.getClass(), targetClazz);
            return Mono.just(converted);
        }
    }
}
