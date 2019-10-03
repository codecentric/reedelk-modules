package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateScriptFunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptBlock;
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
    public <T> Optional<T> evaluate(ScriptBlock script, Message message, FlowContext flowContext, Class<T> returnType) {
        if (script == null || script.isEmpty()) {
            return OPTIONAL_PROVIDER.empty();
        } else {
            return (Optional<T>) evaluateScript(script, message, flowContext, returnType, OPTIONAL_PROVIDER);
        }
    }

    @Override
    public <T> Publisher<T> evaluateStream(ScriptBlock script, Message message, FlowContext flowContext, Class<T> returnType) {
        if (script == null || script.isEmpty()) {
            return STREAM_PROVIDER.empty();
        } else if (script.isEvaluateMessagePayload()) {
            return evaluateMessagePayload(returnType, message);
        } else {
            return (Publisher<T>) evaluateScript(script, message, flowContext, returnType, STREAM_PROVIDER);
        }
    }

    private <T> T evaluateScript(ScriptBlock script, Message message, FlowContext flowContext, Class<T> returnType, ValueProvider valueProvider) {
        String functionName = functionNameOf(script, FUNCTION);
        Object evaluationResult = scriptEngine.invokeFunction(functionName, message, flowContext);
        return convert(evaluationResult, returnType, valueProvider);
    }
}
