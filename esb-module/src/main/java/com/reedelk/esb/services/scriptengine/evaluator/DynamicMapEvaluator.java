package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.converter.DefaultConverterService;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateDynamicMapFunctionDefinitionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicmap.DynamicMap;

import java.util.Collections;
import java.util.Map;

public class DynamicMapEvaluator extends AbstractDynamicValueEvaluator {

    private static final FunctionDefinitionBuilder MAP_FUNCTION_BUILDER = new EvaluateDynamicMapFunctionDefinitionBuilder();
    private static final Map<String,?> EMPTY_MAP = Collections.unmodifiableMap(Collections.emptyMap());

    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, Message message, FlowContext context) {
        if (dynamicMap == null || dynamicMap.isEmpty()) {
            // If dynamic map is empty, nothing to do.
            return (Map<String, T>) EMPTY_MAP;

        } else {

            String functionName = functionNameOf(dynamicMap, MAP_FUNCTION_BUILDER);

            Map<String, T> evaluatedMap = (Map<String, T>) scriptEngine().invokeFunction(functionName, message, context);

            // We map the values to the correct output value type
            evaluatedMap.forEach((key, value) -> {
                T converted = DefaultConverterService.getInstance().convert(value, dynamicMap.getEvaluatedType());
                evaluatedMap.put(key, converted);
            });

            return evaluatedMap;
        }
    }
}
