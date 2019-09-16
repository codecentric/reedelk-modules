package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.commons.ScriptUtils;

import java.util.Map;

public class MapAssignment<T> implements VariableAssignment {

    private final String name;
    private final Map<String,T> map;

    private MapAssignment(String name, Map<String,T> map) {
        this.name = name;
        this.map = map;
    }

    public static <T> MapAssignment<T> from(String name, Map<String,T> map) {
        return new MapAssignment<>(name, map);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String script() {
        return asScript();
    }

    private String asScript() {
        StringBuilder builder = new StringBuilder();
        builder.append("var ").append(name).append(" = ").append("{");
        for (Map.Entry<String,T> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String && ScriptUtils.isScript((String) value)) {
                value = ScriptUtils.unwrap((String) value);
            } else if (value instanceof String) {
                value = "'" + value + "'";
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
        return builder.toString();
    }
}
