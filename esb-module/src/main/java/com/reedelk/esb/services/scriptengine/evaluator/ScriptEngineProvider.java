package com.reedelk.esb.services.scriptengine.evaluator;

import java.io.Reader;
import java.util.Collection;
import java.util.Map;

public interface ScriptEngineProvider {

    void eval(Collection<String> modules, Reader reader, Map<String,Object> bindings);

    void eval(String functionDefinition);

    void clear(String module);

    Object invokeFunction(String functionName, Object ...args);
}
