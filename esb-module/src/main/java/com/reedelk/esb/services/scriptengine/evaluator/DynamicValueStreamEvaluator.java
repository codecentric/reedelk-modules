package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import reactor.core.publisher.Mono;

import static com.reedelk.esb.services.scriptengine.evaluator.ValueProviders.STREAM_PROVIDER;

public class DynamicValueStreamEvaluator extends AbstractDynamicValueEvaluator {

    public DynamicValueStreamEvaluator(ScriptEngineProvider provider) {
        super(provider);
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        if (dynamicValue == null) {
            // Value is not present
            return null;
        } else if (dynamicValue.isScript()) {
            // Script
            if (dynamicValue.isEvaluateMessagePayload()) {
                return evaluateMessagePayload(dynamicValue.getEvaluatedType(), message);
            } else {
                return TypedPublisher.from(execute(dynamicValue, STREAM_PROVIDER, FUNCTION, message, flowContext), dynamicValue.getEvaluatedType());
            }
        } else {
            // Not a script
            return TypedPublisher.from(Mono.justOrEmpty(dynamicValue.getValue()), dynamicValue.getEvaluatedType());
        }
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(DynamicValue<T> dynamicValue, Throwable throwable, FlowContext flowContext) {
        if (dynamicValue == null) {
            // Value is not present
            return null;
        } else if (dynamicValue.isScript()) {
            // Script
            return TypedPublisher.from(
                    execute(dynamicValue, STREAM_PROVIDER, ERROR_FUNCTION, throwable, flowContext),
                    dynamicValue.getEvaluatedType());
        } else {
            // Not a script
            return TypedPublisher.from(Mono.justOrEmpty(dynamicValue.getValue()), dynamicValue.getEvaluatedType());
        }
    }
}
