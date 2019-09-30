package com.reedelk.esb.services.scriptengine.evaluator;

public interface ScriptEngineProvider {

    Object invokeFunction(String functionName, Object ...args);

    void eval(String functionDefinition);
}
