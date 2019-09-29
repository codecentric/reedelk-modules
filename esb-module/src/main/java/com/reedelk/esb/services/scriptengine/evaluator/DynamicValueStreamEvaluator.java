package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicValue;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.script.Invocable;
import javax.script.ScriptEngine;

public class DynamicValueStreamEvaluator extends AbstractDynamicValueEvaluator {

    public DynamicValueStreamEvaluator(ScriptEngine engine, Invocable invocable) {
        super(engine, invocable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        if (dynamicValue == null) {
            return (Publisher<T>) PROVIDER.empty();
        } else if (dynamicValue.isScript()) {
            if (dynamicValue.isEvaluateMessagePayload()) {
                if (message.getContent().isStream()) {
                    // We don't resolve the stream
                    Publisher<?> stream = message.getContent().stream();
                    Class<?> targetType = dynamicValue.getEvaluatedType();
                    Class<?> streamType = message.getContent().type().getTypeClass();
                    return (Publisher<T>) convert(stream, streamType, targetType, PROVIDER);
                } else {
                    // We avoid evaluating the payload (optimization)
                    return (Publisher<T>) convert(message.payload(), dynamicValue.getEvaluatedType(), PROVIDER);
                }
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

    private final ValueProvider<Publisher<?>> PROVIDER = new StreamValueProvider();

    class StreamValueProvider implements ValueProvider<Publisher<?>> {
        @Override
        public Publisher<?> empty() {
            return Mono.empty();
        }

        @Override
        public Publisher<?> from(Object value) {
            return Mono.just(value);
        }
    }
}
