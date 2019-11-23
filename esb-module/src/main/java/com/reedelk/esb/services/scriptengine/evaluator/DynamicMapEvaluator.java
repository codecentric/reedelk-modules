package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.converter.DefaultConverterService;
import com.reedelk.esb.services.scriptengine.evaluator.function.DynamicMapWithMessageAndContext;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicmap.DynamicMap;

import java.util.Collections;
import java.util.Map;

public class DynamicMapEvaluator extends AbstractDynamicValueEvaluator {

    private final FunctionDefinitionBuilder mapFunctionBuilder = new DynamicMapWithMessageAndContext();
    private final Map<String,?> emptyMap = Collections.unmodifiableMap(Collections.emptyMap());

    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, FlowContext context, Message message) {
        if (dynamicMap == null || dynamicMap.isEmpty()) {
            // If dynamic map is empty, nothing to do.
            return (Map<String, T>) emptyMap;

        } else {
            Map<String, T> evaluatedMap = (Map<String, T>) invokeFunction(dynamicMap, mapFunctionBuilder, message, context);
            evaluatedMap.forEach((key, value) -> {
                // We map the values to the correct output value type
                T converted = DefaultConverterService.getInstance().convert(value, dynamicMap.getEvaluatedType());
                evaluatedMap.put(key, converted);
            });
            return evaluatedMap;
        }
    }
}
