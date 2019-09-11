package com.reedelk.esb.execution;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.type.TypedContent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultFlowContext extends HashMap<String, TypedContent<?>> implements FlowContext {

    @Override
    public void setVariable(String variableName, TypedContent<?> variableValue) {
        put(variableName, variableValue);
    }

    @Override
    public void removeVariable(String variableName) {
        remove(variableName);
    }

    @Override
    public TypedContent<?> getVariable(String variableName) {
        return get(variableName);
    }

    @Override
    public Map<String, TypedContent<?>> variablesMap() {
        return Collections.unmodifiableMap(this);
    }
}
