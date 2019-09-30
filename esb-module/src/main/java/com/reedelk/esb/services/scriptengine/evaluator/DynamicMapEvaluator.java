package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicMap;

import java.util.Collections;
import java.util.Map;

public class DynamicMapEvaluator extends AbstractDynamicValueEvaluator {

    private static final Map<String,?> EMPTY_MAP = Collections.unmodifiableMap(Collections.emptyMap());

    public DynamicMapEvaluator(ScriptEngineProvider provider) {
        super(provider);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, Message message, FlowContext context) {
        if (dynamicMap.isEmpty()) {
            // If dynamic map is empty, nothing to do.
            return (Map<String, T>) EMPTY_MAP;
        } else {
            String functionName = functionNameOf(dynamicMap);
            return (Map<String, T>) scriptEngine.invokeFunction(functionName, message, context);
        }
    }

    private <T> String functionNameOf(DynamicMap<T> dynamicMap) {
        String valueUUID =  dynamicMap.getUUID();
        String functionName = uuidFunctionNameMap.getOrDefault(valueUUID, null);
        if (functionName == null) {
            synchronized (this) {
                if (!uuidFunctionNameMap.containsKey(valueUUID)) {
                    functionName = functionNameFrom(valueUUID);
                    EvaluateMapFunction<T> evaluateMapFunction = new EvaluateMapFunction<>(functionName, dynamicMap);
                    String functionDefinition = evaluateMapFunction.script();
                    scriptEngine.eval(functionDefinition);
                    uuidFunctionNameMap.put(valueUUID, functionName);
                }
            }
        }
        return functionName;
    }
}
