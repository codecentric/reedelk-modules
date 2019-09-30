package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicValue;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@SuppressWarnings("unchecked")
public class DynamicValueStreamEvaluator extends AbstractDynamicValueEvaluator {

    public DynamicValueStreamEvaluator(ScriptEngineProvider provider) {
        super(provider);
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        if (dynamicValue == null) {
            return (Publisher<T>) PROVIDER.empty();
        } else if (dynamicValue.isScript()) {
            if (dynamicValue.isEvaluateMessagePayload()) {
                // We avoid evaluating the payload (optimization)
                return evaluateMessagePayload(dynamicValue, message);
            } else {
                return (Publisher<T>) execute(dynamicValue, PROVIDER, FUNCTION, message, flowContext);
            }
        } else {
            // Not a script
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getBody(), String.class, dynamicValue.getEvaluatedType());
            return (Publisher<T>) PROVIDER.from(converted);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Throwable throwable, FlowContext flowContext) {
        if (dynamicValue == null) {
            return (Publisher<T>) PROVIDER.empty();
        } else if (dynamicValue.isScript()) {

            // Script
            return (Publisher<T>) execute(dynamicValue, PROVIDER, ERROR_FUNCTION, throwable, flowContext);

        } else {
            // Not a script
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getBody(), String.class, dynamicValue.getEvaluatedType());
            return (Publisher<T>) PROVIDER.from(converted);
        }
    }

    private <T> Publisher<T> evaluateMessagePayload(DynamicValue<T> dynamicValue, Message message) {
        if (message.getContent().isStream()) {
            // We don't resolve the stream, but we still might need to
            // map its content from source type to a target type.
            Publisher<?> stream = message.getContent().stream();
            Class<?> targetType = dynamicValue.getEvaluatedType();
            Class<?> sourceType = message.getContent().streamType();
            return (Publisher<T>) convert(stream, sourceType, targetType, PROVIDER);
        } else {
            return (Publisher<T>) convert(message.payload(), dynamicValue.getEvaluatedType(), PROVIDER);
        }
    }

    private final ValueProvider<Publisher<?>> PROVIDER = new StreamValueProvider();

    class StreamValueProvider implements ValueProvider<Publisher<?>> {
        @Override
        public Publisher<?> empty() {
            return Mono.empty();
        }

        @Override
        public Publisher<?> from(Object value) {
            return value instanceof Publisher ? (Publisher<?>) value : Mono.just(value);
        }
    }
}
