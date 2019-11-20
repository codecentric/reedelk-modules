package com.reedelk.esb.services.scriptengine;

import com.reedelk.esb.services.scriptengine.evaluator.*;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.script.ScriptSource;
import com.reedelk.runtime.api.script.dynamicmap.DynamicMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicObject;
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
    private FunctionRegister functionRegister;
    private ScriptEvaluator scriptEvaluator;

    private ScriptEngine() {
        dynamicValueStreamEvaluator = new DynamicValueStreamEvaluator();
        dynamicValueEvaluator = new DynamicValueEvaluator();
        dynamicMapEvaluator = new DynamicMapEvaluator();
        functionRegister = new FunctionRegister();
        scriptEvaluator = new ScriptEvaluator();
    }

    // Dynamic value

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        return dynamicValueEvaluator.evaluate(dynamicValue, message, flowContext);
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Throwable exception, FlowContext flowContext) {
        return dynamicValueEvaluator.evaluate(dynamicValue, exception, flowContext);
    }

    @Override
    public <T> Optional<T> evaluate(DynamicObject dynamicObject, MimeType mimeType, Message message, FlowContext flowContext) {
        return dynamicValueEvaluator.evaluate(dynamicObject, mimeType, message, flowContext);
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(DynamicValue<T> dynamicValue, Throwable throwable, FlowContext flowContext) {
        return dynamicValueStreamEvaluator.evaluateStream(dynamicValue, throwable, flowContext);
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        return dynamicValueStreamEvaluator.evaluateStream(dynamicValue, message, flowContext);
    }

    // Script

    @Override
    public <T> Optional<T> evaluate(Script script, List<Message> messages, FlowContext flowContext, Class<T> returnType) {
        return scriptEvaluator.evaluate(script, messages, flowContext, returnType);
    }

    @Override
    public <T> Optional<T> evaluate(Script script, Message message, FlowContext flowContext, Class<T> returnType) {
        return scriptEvaluator.evaluate(script, message, flowContext, returnType);
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(Script script, Message message, FlowContext flowContext, Class<T> returnType) {
        return scriptEvaluator.evaluateStream(script, message, flowContext, returnType);
    }

    // Dynamic map

    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, Message message, FlowContext context) {
        return dynamicMapEvaluator.evaluate(dynamicMap, message, context);
    }

    // Register Function

    @Override
    public void registerFunction(ScriptSource scriptSource) {
        functionRegister.registerFunction(scriptSource);
    }

    @Override
    public void onDisposed(Component component) {
        dynamicValueStreamEvaluator.onDisposed(component);
        dynamicValueEvaluator.onDisposed(component);
        dynamicMapEvaluator.onDisposed(component);
        scriptEvaluator.onDisposed(component);
    }
}