package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@SuppressWarnings("unchecked")
public enum ESBJavascriptEngine implements ScriptEngineService {

    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(ESBJavascriptEngine.class);

    private static final String ENGINE_NAME = "nashorn";

    private final ScriptEngine engine;

    ESBJavascriptEngine() {
        engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
    }

    @Override
    public <T> T evaluate(String script, FlowContext flowContext, Bindings additionalBindings) {
        SimpleBindings bindings = bindingsFromContext(flowContext);
        bindings.putAll(additionalBindings);
        return _eval(script, bindings);
    }

    @Override
    public <T> T evaluate(String script, FlowContext flowContext) {
        SimpleBindings bindings = bindingsFromContext(flowContext);
        return _eval(script, bindings);
    }

    @Override
    public <T> T evaluate(String script, Message message) {
        DefaultContextVariables defaultContextVariables = new DefaultContextVariables(message);
        return _eval(script, defaultContextVariables);
    }

    @Override
    public <T> T evaluate(String script, Message message, FlowContext flowContext) {
        SimpleBindings bindings = bindingsFromContext(flowContext);
        DefaultContextVariables defaultContextVariables = new DefaultContextVariables(message);
        defaultContextVariables.putAll(bindings);
        return _eval(script, defaultContextVariables);
    }

    @Override
    public DefaultScriptExecutionResult evaluate(String script, Message message, Bindings additionalBindings) {
        // TODO: I think that this one creates side effects,
        //  this code should be revised. Bindings should be removed afterwards?
        Bindings existingBindings = engine.createBindings();
        existingBindings.putAll(new DefaultContextVariables(message));
        existingBindings.putAll(additionalBindings);

        Object evaluated = _eval(script, existingBindings);
        return new DefaultScriptExecutionResult(evaluated, existingBindings);
    }

    @Override
    public DefaultScriptExecutionResult evaluate(List<Message> messages, String script) {
        JoinContextVariables defaultContextVariables = new JoinContextVariables(messages);

        Bindings bindings = engine.createBindings();
        bindings.putAll(defaultContextVariables);

        Object evaluated = _eval(script, bindings);
        return new DefaultScriptExecutionResult(evaluated, bindings);
    }

    private SimpleBindings bindingsFromContext(FlowContext flowContext) {
        SimpleBindings bindings = new SimpleBindings();
        Map<String, TypedContent<?>> variablesMap = flowContext.variablesMap();
        for (Map.Entry<String, TypedContent<?>> variable : variablesMap.entrySet()) {
            bindings.put(variable.getKey(), variable.getValue().data());
        }
        return bindings;
    }

    private <T> T _eval(String script, Bindings bindings) {
        String evaluate = ScriptUtils.unwrap(script);
        try {
            return (T) engine.eval(evaluate, bindings);
        } catch (ScriptException e) {
            logger.error(format("error valuating script='%s'", script), e);
            throw new ESBException(e);
        }
    }
}
