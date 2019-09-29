package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicValue;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

public class DynamicValueEvaluator extends ScriptEngineServiceAdapter {

    private static final String INLINE_ERROR_SCRIPT =
            "var %s = function(error, context) {\n" +
                    "  return %s\n" +
                    "};";


    private static final String INLINE_SCRIPT =
            "var %s = function(message, context) {\n" +
                    "  return %s\n" +
                    "};";

    private final Map<String, String> ORIGIN_FUNCTION_NAME = new HashMap<>();

    private final ScriptEngine engine;
    private final Invocable invocable;

    public DynamicValueEvaluator(ScriptEngine engine, Invocable invocable) {
        this.engine = engine;
        this.invocable = invocable;
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        if (dynamicValue == null) return Optional.empty();

        if (dynamicValue.isScript()) {

            if (dynamicValue.isEmptyScript()) {
                // If script is empty, no need to evaluate it.
                return Optional.empty();
            } else if (dynamicValue.isEvaluateMessagePayload()) {
                return evaluateMessagePayloadOptimization(dynamicValue.getEvaluatedType(), message);
                // Script
            } else {
                return execute(dynamicValue, INLINE_SCRIPT, message, flowContext);
            }

        } else {
            // Not a script
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getBody(), String.class, dynamicValue.getEvaluatedType());
            return Optional.ofNullable(converted);
        }
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Throwable exception, FlowContext flowContext) {
        if (dynamicValue == null) {
            return Optional.empty();

            // Script
        } else if (dynamicValue.isScript()) {
            return dynamicValue.isEmptyScript() ?
                    Optional.empty() : // If script is empty, no need to evaluate it.
                    execute(dynamicValue, INLINE_ERROR_SCRIPT, exception, flowContext);
        } else {
            // Not a script
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getBody(), String.class, dynamicValue.getEvaluatedType());
            return Optional.ofNullable(converted);
        }
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Throwable throwable, FlowContext flowContext) {
        return Mono.empty();
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        return Mono.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> execute(DynamicValue<T> dynamicValue, String template, Object... args) {
        String functionName = functionNameOf(dynamicValue, template);

        Object evaluationResult;
        try {
            evaluationResult = invocable.invokeFunction(functionName, args);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new ESBException(e);
        }

        if (evaluationResult == null) {
            return Optional.empty();
        } else if (dynamicValue.getEvaluatedType().isAssignableFrom(evaluationResult.getClass())) {
            return Optional.of((T) evaluationResult);
        } else {
            // If the evaluation result does not have the desired type, we try to convert it
            // to the desired type.
            T converted = DynamicValueConverterFactory.convert(evaluationResult, evaluationResult.getClass(), dynamicValue.getEvaluatedType());
            return Optional.ofNullable(converted);
        }
    }

    private <T> String functionNameOf(DynamicValue<T> dynamicValue, String template) {
        String valueUUID =  dynamicValue.getUUID();
        String functionName = ORIGIN_FUNCTION_NAME.getOrDefault(valueUUID, null);
        if (functionName == null) {
            synchronized (this) {
                if (functionName == null) {
                    functionName = "fun" + valueUUID;
                    String scriptBody = dynamicValue.getBody();
                    String functionDefinition = format(template, functionName, ScriptUtils.unwrap(scriptBody));
                    try {
                        // Compiling the function definition.
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

    private static <T> Optional<T> evaluateMessagePayloadOptimization(Class<T> targetDesiredClass, Message message) {
        // We avoid interpreting the message payload in javascript (this is an optimization)...
        T payload = message.payload();
        if (payload == null) {
            return Optional.empty();
        } else {
            // Convert the payload to the desired type
            T converted = DynamicValueConverterFactory.convert(payload, payload.getClass(), targetDesiredClass);
            return Optional.ofNullable(converted);
        }
    }
}
