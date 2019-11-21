package com.reedelk.esb.services.scriptengine;

import com.reedelk.esb.services.scriptengine.evaluator.*;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.script.ScriptSource;
import com.reedelk.runtime.api.script.dynamicmap.DynamicMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import com.reedelk.runtime.api.service.ScriptEngineService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScriptEngine implements ScriptEngineService {

    public static final ScriptEngine INSTANCE = new ScriptEngine();

    private DynamicValueStreamEvaluator dynamicValueStreamEvaluator;
    private DynamicValueEvaluator dynamicValueEvaluator;
    private DynamicMapEvaluator dynamicMapEvaluator;
    private ScriptSourceEvaluator scriptSourceEvaluator;
    private ScriptEvaluator scriptEvaluator;

    private ScriptEngine() {
        dynamicValueStreamEvaluator = new DynamicValueStreamEvaluator();
        dynamicValueEvaluator = new DynamicValueEvaluator();
        dynamicMapEvaluator = new DynamicMapEvaluator();
        scriptSourceEvaluator = new ScriptSourceEvaluator();
        scriptEvaluator = new ScriptEvaluator();
    }

    // Dynamic value

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, FlowContext flowContext, Message message) {
        return dynamicValueEvaluator.evaluate(dynamicValue, flowContext, message);
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, FlowContext flowContext, Throwable exception) {
        return dynamicValueEvaluator.evaluate(dynamicValue, flowContext, exception);
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicObject, FlowContext flowContext, Message message, MimeType mimeType) {
        return dynamicValueEvaluator.evaluate(dynamicObject, flowContext, message, mimeType);
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(DynamicValue<T> dynamicValue, FlowContext flowContext, Throwable throwable) {
        return dynamicValueStreamEvaluator.evaluateStream(dynamicValue, flowContext, throwable);
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(DynamicValue<T> dynamicValue, FlowContext flowContext, Message message) {
        return dynamicValueStreamEvaluator.evaluateStream(dynamicValue, flowContext, message);
    }

    // Script

    @Override
    public <T> Optional<T> evaluate(Script script, FlowContext flowContext, List<Message> messages, Class<T> returnType) {
        return scriptEvaluator.evaluate(script, flowContext, messages, returnType);
    }

    @Override
    public <T> Optional<T> evaluate(Script script, FlowContext flowContext, Message message, Class<T> returnType) {
        return scriptEvaluator.evaluate(script, flowContext, message, returnType);
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(Script script, FlowContext flowContext, Message message, Class<T> returnType) {
        return scriptEvaluator.evaluateStream(script, flowContext, message, returnType);
    }

    // Dynamic map

    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, FlowContext context, Message message) {
        return dynamicMapEvaluator.evaluate(dynamicMap, context, message);
    }

    // Register Function

    @Override
    public void register(ScriptSource scriptSource) {
        scriptSourceEvaluator.register(scriptSource);
    }

}