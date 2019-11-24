package com.reedelk.esb.services.scriptengine.evaluator;

import javax.script.ScriptException;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;

public interface ScriptEngineProvider {

    void compile(String functionDefinition) throws ScriptException;

    void compile(Collection<String> modules, Reader reader, Map<String,Object> bindings) throws ScriptException;

    // TODO: merge this remove module with remove function?
    void removeModule(String module);

    void removeFunction(String functionName);

    Object invokeFunction(String functionName, Object ...args) throws NoSuchMethodException, ScriptException;
}
