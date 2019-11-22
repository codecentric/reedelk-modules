package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.ScriptWithMessageAndContext;
import com.reedelk.esb.services.scriptengine.evaluator.function.ScriptWithMessagesAndContext;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.Script;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;

import static com.reedelk.esb.services.scriptengine.evaluator.ValueProviders.OPTIONAL_PROVIDER;
import static com.reedelk.esb.services.scriptengine.evaluator.ValueProviders.STREAM_PROVIDER;

@SuppressWarnings("unchecked")
public class ScriptEvaluator extends AbstractDynamicValueEvaluator {

    private final FunctionDefinitionBuilder scriptWithMessageAndContext;
    private final FunctionDefinitionBuilder scriptWithMessagesAndContext; // used for Join component with multiple messages as input.

    public ScriptEvaluator() {
        scriptWithMessageAndContext = new ScriptWithMessageAndContext();
        scriptWithMessagesAndContext = new ScriptWithMessagesAndContext();
    }

    @Override
    public <T> Optional<T> evaluate(Script script, FlowContext flowContext, Message message, Class<T> returnType) {
        if (script == null || script.isEmpty()) {
            return OPTIONAL_PROVIDER.empty();
        } else {
            return (Optional<T>) evaluateScript(script, message, flowContext, returnType, OPTIONAL_PROVIDER);
        }
    }

    @Override
    public <T> Optional<T> evaluate(Script script, FlowContext flowContext, List<Message> messages, Class<T> returnType) {
        if (script == null || script.isEmpty()) {
            return OPTIONAL_PROVIDER.empty();
        } else {
            return (Optional<T>) evaluateScript(script, messages, flowContext, returnType, OPTIONAL_PROVIDER);
        }
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(Script script, FlowContext flowContext, Message message, Class<T> returnType) {
        if (script == null) {
            return null;
        } else if (script.isEmpty()) {
            return TypedPublisher.from(STREAM_PROVIDER.empty(), returnType);
        } else {
            Publisher<T> resultPublisher = (Publisher<T>) evaluateScript(script, message, flowContext, returnType, STREAM_PROVIDER);
            return TypedPublisher.from(resultPublisher, returnType);
        }
    }

    private <T> T evaluateScript(Script script, Message message, FlowContext flowContext, Class<T> returnType, ValueProvider valueProvider) {
        String functionName = functionNameOf(script, scriptWithMessageAndContext);
        Object evaluationResult = scriptEngine().invokeFunction(functionName, message, flowContext);
        return convert(evaluationResult, returnType, valueProvider);
    }

    private <T> T evaluateScript(Script script, List<Message> messages, FlowContext flowContext, Class<T> returnType, ValueProvider valueProvider) {
        String functionName = functionNameOf(script, scriptWithMessagesAndContext);
        Object evaluationResult = scriptEngine().invokeFunction(functionName, messages, flowContext);
        return convert(evaluationResult, returnType, valueProvider);
    }
}
