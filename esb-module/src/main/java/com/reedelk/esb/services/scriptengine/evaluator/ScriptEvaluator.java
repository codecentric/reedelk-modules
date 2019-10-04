package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateScriptFunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import com.reedelk.runtime.api.script.Script;
import org.reactivestreams.Publisher;

import java.util.Optional;

import static com.reedelk.esb.services.scriptengine.evaluator.ValueProviders.OPTIONAL_PROVIDER;
import static com.reedelk.esb.services.scriptengine.evaluator.ValueProviders.STREAM_PROVIDER;

@SuppressWarnings("unchecked")
public class ScriptEvaluator extends AbstractDynamicValueEvaluator {

    private static final FunctionDefinitionBuilder FUNCTION = new EvaluateScriptFunctionDefinitionBuilder();

    public ScriptEvaluator(ScriptEngineProvider provider) {
        super(provider);
    }

    @Override
    public <T> Optional<T> evaluate(Script script, Message message, FlowContext flowContext, Class<T> returnType) {
        if (script == null || script.isEmpty()) {
            return OPTIONAL_PROVIDER.empty();
        } else {
            return (Optional<T>) evaluateScript(script, message, flowContext, returnType, OPTIONAL_PROVIDER);
        }
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(Script script, Message message, FlowContext flowContext, Class<T> returnType) {
        if (script == null) {
            return null;
        } else if (script.isEmpty()) {
            return TypedPublisher.from(STREAM_PROVIDER.empty(), returnType);
        } else if (script.isEvaluateMessagePayload()) {
            return TypedPublisher.from(evaluateMessagePayload(returnType, message), returnType);
        } else {
            Publisher<T> resultPublisher = (Publisher<T>) evaluateScript(script, message, flowContext, returnType, STREAM_PROVIDER);
            return TypedPublisher.from(resultPublisher, returnType);
        }
    }

    private <T> T evaluateScript(Script script, Message message, FlowContext flowContext, Class<T> returnType, ValueProvider valueProvider) {
        String functionName = functionNameOf(script, FUNCTION);
        Object evaluationResult = scriptEngine.invokeFunction(functionName, message, flowContext);
        return convert(evaluationResult, returnType, valueProvider);
    }
}
