package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
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
            // Script
            return dynamicValue.isEvaluateMessagePayload() ?
                    evaluateMessagePayload(dynamicValue, message) :
                    (Publisher<T>) execute(dynamicValue, PROVIDER, FUNCTION, message, flowContext);

        } else {
            // Not a script
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getValue(), dynamicValue.getEvaluatedType());
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
            T converted = DynamicValueConverterFactory.convert(dynamicValue.getValue(), dynamicValue.getEvaluatedType());
            return (Publisher<T>) PROVIDER.from(converted);
        }
    }

    /**
     * Evaluate the payload without invoking the script engine. This is an optimization
     * since we can get the payload directly from Java without making an expensive call
     * to the script engine.
     */
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

    private static final ValueProvider<Publisher<?>> PROVIDER = new StreamValueProvider();

    static class StreamValueProvider implements ValueProvider<Publisher<?>> {
        @Override
        public Publisher<?> empty() {
            return Mono.empty();
        }

        @Override
        public Publisher<?> from(Object value) {
            if (value == null) {
                return Mono.empty();
            } else if (value instanceof Publisher<?>) {
                return (Publisher<?>) value;
            } else {
                return Mono.just(value);
            }
        }
    }
}
