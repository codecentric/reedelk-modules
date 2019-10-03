package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionBuilder;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptBlock;
import org.reactivestreams.Publisher;

import java.util.Optional;

public class ScriptEvaluator extends AbstractDynamicValueEvaluator {

    private static final FunctionBuilder FUNCTION = new EvaluateFunctionBuilder();

    public ScriptEvaluator(ScriptEngineProvider provider) {
        super(provider);
    }

    @Override
    public <T> Optional<T> evaluate(ScriptBlock script, Message message, FlowContext flowContext, Class<T> returnType) {
        if (script == null || script.isEmptyScript()) {
            // Script is null or empty
            return OPTIONAL_PROVIDER.empty();
        } else {
            // Script
            String functionName = functionNameOf(script, FUNCTION);
            Object evaluationResult = scriptEngine.invokeFunction(functionName, message, flowContext);
            return convert(evaluationResult, returnType, OPTIONAL_PROVIDER);
        }
    }

    @Override
    public <T> Publisher<T> evaluateStream(ScriptBlock script, Message message, FlowContext flowContext, Class<T> returnType) {
        if (script == null || script.isEmptyScript()) {
            // Script is null or empty
            return STREAM_PROVIDER.empty();
        } else {
            // Script
            String functionName = functionNameOf(script, FUNCTION);
            Object evaluationResult = scriptEngine.invokeFunction(functionName, message, flowContext);
            return convert(evaluationResult, returnType, STREAM_PROVIDER);
        }
    }
}
