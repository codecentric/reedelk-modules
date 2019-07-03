package com.esb.services.scriptengine;

import com.esb.api.message.Message;
import com.esb.api.service.ScriptEngineService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public enum ESBJavascriptEngine implements ScriptEngineService {

    INSTANCE;

    private static final String ENGINE_NAME = "nashorn";

    private final ScriptEngine engine;

    ESBJavascriptEngine() {
        engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
    }

    @Override
    public <T> T evaluate(Message message, String script, Class<T> returnType) throws ScriptException {
        ContextVariables contextVariables = new ContextVariables(message);
        return (T) engine.eval(script, contextVariables);
    }

    @Override
    public Object evaluate(Message message, String script) throws ScriptException {
        ContextVariables contextVariables = new ContextVariables(message);
        return engine.eval(script, contextVariables);
    }
}
