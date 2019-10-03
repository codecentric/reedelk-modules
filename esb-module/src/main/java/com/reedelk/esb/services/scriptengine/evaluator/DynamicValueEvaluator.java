package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;

import java.util.Optional;

import static com.reedelk.esb.services.scriptengine.evaluator.ValueProviders.OPTIONAL_PROVIDER;

public class DynamicValueEvaluator extends AbstractDynamicValueEvaluator {

    public DynamicValueEvaluator(ScriptEngineProvider provider) {
        super(provider);
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        if (dynamicValue == null) {
            // Value is not present
            return OPTIONAL_PROVIDER.empty();
        } else if (dynamicValue.isScript()) {
            // Script
            return dynamicValue.isEvaluateMessagePayload() ?
                    // we avoid evaluating the payload with the script engine (optimization)
                    convert(message.payload(), dynamicValue.getEvaluatedType(), OPTIONAL_PROVIDER) :
                    execute(dynamicValue, OPTIONAL_PROVIDER, FUNCTION, message, flowContext);
        } else {
            // Not a script
            return Optional.ofNullable(dynamicValue.getValue());
        }
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Throwable exception, FlowContext flowContext) {
        if (dynamicValue == null) {
            // Value is not present
            return OPTIONAL_PROVIDER.empty();
        } else if (dynamicValue.isScript()) {
            // Script
            return execute(dynamicValue, OPTIONAL_PROVIDER, ERROR_FUNCTION, exception, flowContext);
        } else {
            // Not a script
            return Optional.ofNullable(dynamicValue.getValue());
        }
    }
}
