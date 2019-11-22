package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.evaluator.function.DynamicValueWithErrorAndContext;
import com.reedelk.esb.services.scriptengine.evaluator.function.DynamicValueWithMessageAndContext;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.commons.JavaType;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;

import java.util.Optional;

import static com.reedelk.esb.services.scriptengine.evaluator.ValueProviders.OPTIONAL_PROVIDER;

public class DynamicValueEvaluator extends AbstractDynamicValueEvaluator {

    private final FunctionDefinitionBuilder<DynamicValue> ERROR_FUNCTION = new DynamicValueWithErrorAndContext();
    private final FunctionDefinitionBuilder<DynamicValue> FUNCTION = new DynamicValueWithMessageAndContext();

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, FlowContext flowContext, Message message) {
        if (dynamicValue == null) {
            // Value is not present
            return OPTIONAL_PROVIDER.empty();
        } else if (dynamicValue.isScript()) {
            // Script
            if (dynamicValue.isEvaluateMessagePayload()) {
                // we avoid evaluating the payload with the script engine (optimization)
                // note that by calling message.payload(), if it is a stream we are
                // automatically resolving it.
                Object payload = message.payload();
                return convert(payload, dynamicValue.getEvaluatedType(), OPTIONAL_PROVIDER);
            } else {
                return execute(dynamicValue, OPTIONAL_PROVIDER, FUNCTION, message, flowContext);
            }

        } else {
            // Not a script
            return Optional.ofNullable(dynamicValue.value());
        }
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, FlowContext flowContext, Throwable exception) {
        if (dynamicValue == null) {
            // Value is not present
            return OPTIONAL_PROVIDER.empty();
        } else if (dynamicValue.isScript()) {
            // Script
            return execute(dynamicValue, OPTIONAL_PROVIDER, ERROR_FUNCTION, exception, flowContext);
        } else {
            // Not a script
            return Optional.ofNullable(dynamicValue.value());
        }
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, FlowContext flowContext, Message message, MimeType mimeType) {
        if (dynamicValue == null) {
            // Value is not present
            return OPTIONAL_PROVIDER.empty();
        } else if (dynamicValue.isScript()) {
            if (dynamicValue.isEmpty()) {
                return OPTIONAL_PROVIDER.empty();
                // Script
            } else if (dynamicValue.isEvaluateMessagePayload()) {
                // we avoid evaluating the payload with the script engine (optimization)
                // note that by calling message.payload(), if it is a stream we are
                // automatically resolving it.
                Object payload = message.payload();
                return convert(payload, JavaType.from(mimeType), OPTIONAL_PROVIDER);
            } else {
                String functionName = functionNameOf(dynamicValue, FUNCTION);
                Object evaluationResult = scriptEngine().invokeFunction(functionName, message, flowContext);
                return convert(evaluationResult, JavaType.from(mimeType), OPTIONAL_PROVIDER);
            }
        } else {
            return convert(dynamicValue.value(), JavaType.from(mimeType), OPTIONAL_PROVIDER);
        }
    }
}
