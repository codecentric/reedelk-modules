package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicValue;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.util.Optional;

public class DynamicValueEvaluator extends AbstractDynamicValueEvaluator {

    public DynamicValueEvaluator(ScriptEngine engine, Invocable invocable) {
        super(engine, invocable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        if (dynamicValue == null) {
            return (Optional<T>) PROVIDER.empty();
            // Script
        } else if (dynamicValue.isScript()) {
            if (dynamicValue.isEvaluateMessagePayload()) {
                // We avoid evaluating the payload (optimization)
                return (Optional<T>) convert(message.payload(), dynamicValue.getEvaluatedType(), PROVIDER);
            } else {
                return (Optional<T>) execute(dynamicValue, PROVIDER, FUNCTION, message, flowContext);
            }
        } else {
            // Not a script
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getBody(), String.class, dynamicValue.getEvaluatedType());
            return (Optional<T>) PROVIDER.from(converted);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Throwable exception, FlowContext flowContext) {
        if (dynamicValue == null) {
            return (Optional<T>) PROVIDER.empty();
            // Script
        } else if (dynamicValue.isScript()) {
            return (Optional<T>) execute(dynamicValue, PROVIDER, ERROR_FUNCTION, exception, flowContext);
            // Not a script
        } else {
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getBody(), String.class, dynamicValue.getEvaluatedType());
            return (Optional<T>) PROVIDER.from(converted);
        }
    }

    private final ValueProvider<Optional<?>> PROVIDER = new OptionalValueProvider();

    class OptionalValueProvider implements ValueProvider<Optional<?>> {
        @Override
        public Optional<?> empty() {
            return Optional.empty();
        }

        @Override
        public Optional<?> from(Object value) {
            return Optional.ofNullable(value);
        }
    }
}
