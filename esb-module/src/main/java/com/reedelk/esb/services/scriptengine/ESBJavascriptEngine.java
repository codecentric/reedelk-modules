package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;

import javax.script.*;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public enum ESBJavascriptEngine implements ScriptEngineService {

    INSTANCE;

    private static final String ENGINE_NAME = "nashorn";

    private final ScriptEngine engine;

    ESBJavascriptEngine() {
        engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
    }


    @Override
    public <T> T evaluate(String script, FlowContext flowContext, Bindings additionalBindings) throws ScriptException {
        SimpleBindings bindings = bindingsFromContext(flowContext);
        bindings.putAll(additionalBindings);
        return (T) engine.eval(script, bindings);
    }

    @Override
    public <T> T evaluate(String script, FlowContext flowContext) throws ScriptException {
        SimpleBindings bindings = bindingsFromContext(flowContext);
        return (T) engine.eval(script, bindings);
    }

    @Override
    public <T> T evaluate(String script, Message message) throws ScriptException {
        DefaultContextVariables defaultContextVariables = new DefaultContextVariables(message);
        return (T) engine.eval(script, defaultContextVariables);
    }

    @Override
    public <T> T evaluate(String script, Message message, FlowContext flowContext) throws ScriptException {
        SimpleBindings bindings = bindingsFromContext(flowContext);
        DefaultContextVariables defaultContextVariables = new DefaultContextVariables(message);
        defaultContextVariables.putAll(bindings);
        return (T) engine.eval(script, defaultContextVariables);
    }

    @Override
    public DefaultScriptExecutionResult evaluate(String script, Message message, Bindings additionalBindings) throws ScriptException {
        // TODO: I think that this one creates side effects, this code should be revised. Bindings should be removed afterwards?
        Bindings existingBindings = engine.createBindings();
        existingBindings.putAll(new DefaultContextVariables(message));
        existingBindings.putAll(additionalBindings);

        Object evaluated = engine.eval(script, existingBindings);
        return new DefaultScriptExecutionResult(evaluated, existingBindings);
    }

    @Override
    public DefaultScriptExecutionResult evaluate(List<Message> messages, String script) throws ScriptException {
        JoinContextVariables defaultContextVariables = new JoinContextVariables(messages);

        Bindings bindings = engine.createBindings();
        bindings.putAll(defaultContextVariables);

        Object evaluated = engine.eval(script, bindings);
        return new DefaultScriptExecutionResult(evaluated, bindings);
    }

    private SimpleBindings bindingsFromContext(FlowContext flowContext) {
        SimpleBindings bindings = new SimpleBindings();
        Map<String, TypedContent<?>> variablesMap = flowContext.variablesMap();
        for (Map.Entry<String, TypedContent<?>> variable : variablesMap.entrySet()) {
            bindings.put(variable.getKey(), variable.getValue().content());
        }
        return bindings;
    }
}
