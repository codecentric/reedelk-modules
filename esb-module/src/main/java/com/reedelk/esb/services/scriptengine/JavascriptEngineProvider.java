package com.reedelk.esb.services.scriptengine;

import com.reedelk.esb.services.scriptengine.evaluator.ScriptEngineProvider;
import com.reedelk.runtime.api.exception.ESBException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavascriptEngineProvider implements ScriptEngineProvider {

    public static final ScriptEngineProvider INSTANCE = new JavascriptEngineProvider();

    private static final String ENGINE_NAME = "nashorn";

    private final ScriptEngine engine;
    private final Invocable invocable;

    private JavascriptEngineProvider() {
        this.engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        this.invocable = (Invocable) engine;
    }

    @Override
    public Object invokeFunction(String functionName, Object... args) {
        try {
            return invocable.invokeFunction(functionName, args);
        } catch (NoSuchMethodException | ScriptException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void eval(String functionDefinition) {
        try {
            engine.eval(functionDefinition);
        } catch (ScriptException e) {
            throw new ESBException(e);
        }
    }
}
