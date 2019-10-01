package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;

import java.util.Optional;

public class DynamicValueEvaluator extends AbstractDynamicValueEvaluator {

    public DynamicValueEvaluator(ScriptEngineProvider provider) {
        super(provider);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        if (dynamicValue == null) {
            return (Optional<T>) PROVIDER.empty();
            // Script
        } else if (dynamicValue.isScript()) {
            return dynamicValue.isEvaluateMessagePayload() ?
                    // we avoid evaluating the payload with the script engine (optimization)
                    (Optional<T>) convert(message.payload(), dynamicValue.getEvaluatedType(), PROVIDER) :
                    (Optional<T>) execute(dynamicValue, PROVIDER, FUNCTION, message, flowContext);
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

    private static final ValueProvider<Optional<?>> PROVIDER = new OptionalValueProvider();

    static class OptionalValueProvider implements ValueProvider<Optional<?>> {
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
