package com.reedelk.esb.services.scriptengine;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicMapEvaluator;
import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueEvaluator;
import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueStreamEvaluator;
import com.reedelk.esb.services.scriptengine.evaluator.ScriptEngineProvider;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;

import java.util.Map;
import java.util.Optional;

public class ScriptEngine implements ScriptEngineService {

    public static final ScriptEngine INSTANCE = new ScriptEngine();

    private DynamicValueStreamEvaluator dynamicValueStreamEvaluator;
    private DynamicValueEvaluator dynamicValueEvaluator;
    private DynamicMapEvaluator dynamicMapEvaluator;

    private ScriptEngine() {
        ScriptEngineProvider provider = JavascriptEngineProvider.INSTANCE;

        dynamicValueStreamEvaluator = new DynamicValueStreamEvaluator(provider);
        dynamicValueEvaluator = new DynamicValueEvaluator(provider);
        dynamicMapEvaluator = new DynamicMapEvaluator(provider);
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        return dynamicValueEvaluator.evaluate(dynamicValue, message, flowContext);
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, Throwable exception, FlowContext flowContext) {
        return dynamicValueEvaluator.evaluate(dynamicValue, exception, flowContext);
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Throwable throwable, FlowContext flowContext) {
        return dynamicValueStreamEvaluator.evaluateStream(dynamicValue, throwable, flowContext);
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> dynamicValue, Message message, FlowContext flowContext) {
        return dynamicValueStreamEvaluator.evaluateStream(dynamicValue, message, flowContext);
    }

    @Override
    public <T> Optional<T> evaluate(Script script, Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException("Implement me");
    }

    @Override
    public <T> Publisher<T> evaluateStream(Script script, Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException("Implement me");
    }

    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, Message message, FlowContext context) {
        return dynamicMapEvaluator.evaluate(dynamicMap, message, context);
    }

    @Override
    public void onDisposed(Component component) {
        dynamicValueStreamEvaluator.onDisposed(component);
        dynamicValueEvaluator.onDisposed(component);
        dynamicMapEvaluator.onDisposed(component);
    }
}