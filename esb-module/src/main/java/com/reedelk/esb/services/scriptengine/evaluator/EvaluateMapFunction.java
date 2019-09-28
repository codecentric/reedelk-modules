package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.DynamicMap;

import java.util.Map;

public class EvaluateMapFunction<T> {

    private static final String EVALUATE_MAP_SCRIPT =
            "var %s = function(message, context) {\n" +
                    "  return %s\n" +
                    "};";

    private final DynamicMap<T> map;
    private final String functionName;

    public EvaluateMapFunction(String functionName, DynamicMap<T> map) {
        this.map = map;
        this.functionName = functionName;
    }

    public String name() {
        return functionName;
    }

    public String script() {
        return asScript();
    }

    /**
     * {
     *  key1:value1,
     *  key2:value2
     * }
     */
    @SuppressWarnings("unchecked")
    private String asScript() {
        StringBuilder builder = new StringBuilder("{");

        for (Map.Entry<String,T> entry : map.entrySet()) {
            String key = entry.getKey();
            T value = entry.getValue();

            if (value instanceof String && ScriptUtils.isScript((String) value)) {
                // If it is a script, we need to unwrap it.
                value = (T) ScriptUtils.unwrap((String) value);
            } else if (value instanceof String) {
                // If it is text we need to surround the values with quotes.
                // TODO: Escape value if  it contains quotes already!
                value = (T) ("'" + value + "'");
            }

            builder.append(key)
                    .append(":")
                    .append(" ")
                    .append(value)
                    .append(",")
                    .append(" ");
        }

        // Remove final space and comma (,) character
        if (!map.isEmpty()) builder.delete(builder.length() - 2, builder.length() - 1);
        builder.append("};");
        return String.format(EVALUATE_MAP_SCRIPT, functionName, builder.toString());
    }
}
