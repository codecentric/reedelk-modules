package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class DynamicValueStreamEvaluator extends AbstractDynamicValueEvaluator {

    public DynamicValueStreamEvaluator(ScriptEngineProvider provider) {
        super(provider);
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        if (dynamicValue == null) {
            // Value is not present
            return STREAM_PROVIDER.empty();
        } else if (dynamicValue.isScript()) {
            // Script
            return dynamicValue.isEvaluateMessagePayload() ?
                    evaluateMessagePayload(dynamicValue, message) :
                    execute(dynamicValue, STREAM_PROVIDER, FUNCTION, message, flowContext);
        } else {
            // Not a script
            return Mono.justOrEmpty(dynamicValue.getValue());
        }
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Throwable throwable, FlowContext flowContext) {
        if (dynamicValue == null) {
            // Value is not present
            return STREAM_PROVIDER.empty();
        } else if (dynamicValue.isScript()) {
            // Script
            return execute(dynamicValue, STREAM_PROVIDER, ERROR_FUNCTION, throwable, flowContext);
        } else {
            // Not a script
            return Mono.justOrEmpty(dynamicValue.getValue());
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
            return convert(stream, sourceType, targetType, STREAM_PROVIDER);
        } else {
            return convert(message.payload(), dynamicValue.getEvaluatedType(), STREAM_PROVIDER);
        }
    }
}
