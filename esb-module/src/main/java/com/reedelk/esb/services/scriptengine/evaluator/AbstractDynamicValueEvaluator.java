package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.DynamicValue;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

abstract class AbstractDynamicValueEvaluator extends ScriptEngineServiceAdapter {

    protected static final String INLINE_ERROR_SCRIPT =
            "var %s = function(error, context) {\n" +
                    "  return %s\n" +
                    "};";


    protected static final String INLINE_SCRIPT =
            "var %s = function(message, context) {\n" +
                    "  return %s\n" +
                    "};";



    private final Map<String, String> ORIGIN_FUNCTION_NAME = new HashMap<>();

    private final ScriptEngine engine;
    protected final Invocable invocable;

    AbstractDynamicValueEvaluator(ScriptEngine engine, Invocable invocable) {
        this.engine = engine;
        this.invocable = invocable;
    }

    <T> String functionNameOf(DynamicValue<T> dynamicValue, String template) {
        String valueUUID =  dynamicValue.getUUID();
        String functionName = ORIGIN_FUNCTION_NAME.getOrDefault(valueUUID, null);
        if (functionName == null) {
            synchronized (this) {
                if (functionName == null) {
                    functionName = "fun" + valueUUID;
                    String scriptBody = dynamicValue.getBody();
                    String functionDefinition = format(template, functionName, ScriptUtils.unwrap(scriptBody));
                    try {
                        // Compiling the function definition.
                        engine.eval(functionDefinition);
                        ORIGIN_FUNCTION_NAME.put(valueUUID, functionName);
                    } catch (ScriptException e) {
                        throw new ESBException(e);
                    }
                }
            }
        }
        return functionName;
    }

    static boolean sourceAssignableToTarget(Class<?> sourceClazz, Class<?> targetClazz) {
        return sourceClazz.isAssignableFrom(targetClazz);
    }
}
