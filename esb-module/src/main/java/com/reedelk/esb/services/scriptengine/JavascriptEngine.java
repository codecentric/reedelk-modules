package com.reedelk.esb.services.scriptengine;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicMapEvaluator;
import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueEvaluator;
import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueStreamEvaluator;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;
import java.util.Optional;

public class JavascriptEngine implements ScriptEngineService {

    public static final JavascriptEngine INSTANCE = new JavascriptEngine();

    private static final String ENGINE_NAME = "nashorn";

    private DynamicValueEvaluator dynamicValueEvaluator;
    private DynamicMapEvaluator dynamicMapEvaluator;
    private DynamicValueStreamEvaluator dynamicValueStreamEvaluator;


    private JavascriptEngine() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        Invocable invocable = (Invocable) engine;

        dynamicValueStreamEvaluator = new DynamicValueStreamEvaluator(engine, invocable);
        dynamicValueEvaluator = new DynamicValueEvaluator(engine, invocable);
        dynamicMapEvaluator = new DynamicMapEvaluator(engine, invocable);
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
        throw new IllegalArgumentException("Not implemented yet.");
    }

    @Override
    public <T> Publisher<T> evaluateStream(Script script, Message message, FlowContext flowContext) {
        return Mono.empty();
    }

    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, Message message, FlowContext context) {
        return dynamicMapEvaluator.evaluate(dynamicMap, message, context);
    }

    @Override
    public void onDisposed(Component component) {
        // TODO: Complete me
        //String key = key(component);
        //ORIGIN_FUNCTION_NAME.remove(key);
    }
}