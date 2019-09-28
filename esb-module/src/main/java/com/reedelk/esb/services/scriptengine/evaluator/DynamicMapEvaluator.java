package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicMap;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DynamicMapEvaluator extends ScriptEngineServiceAdapter {

    private final Map<String, String> ORIGIN_FUNCTION_NAME = new HashMap<>();

    private static final Map<String,?> EMPTY_MAP = Collections.unmodifiableMap(Collections.emptyMap());

    private final ScriptEngine engine;
    private final Invocable invocable;

    public DynamicMapEvaluator(ScriptEngine engine, Invocable invocable) {
        this.engine = engine;
        this.invocable = invocable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, Message message, FlowContext context) {
        if (dynamicMap.isEmpty()) {
            // If dynamic map is empty, nothing to do.
            return (Map<String, T>) EMPTY_MAP;
        } else {
            String functionName = functionNameOf(dynamicMap);
            try {
                return (Map<String, T>) invocable.invokeFunction(functionName, message, context);
            } catch (ScriptException | NoSuchMethodException e) {
                throw new ESBException(e);
            }
        }
    }


    private <T> String functionNameOf(DynamicMap<T> dynamicMap) {
        String valueUUID =  dynamicMap.getUUID();
        String functionName = ORIGIN_FUNCTION_NAME.getOrDefault(valueUUID, null);
        if (functionName == null) {
            synchronized (this) {
                if (functionName == null) {
                    functionName = "fun" + valueUUID;
                    EvaluateMapFunction<T> evaluateMapFunction = new EvaluateMapFunction<>(functionName, dynamicMap);
                    String functionDefinition = evaluateMapFunction.script();
                    try {
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
}
