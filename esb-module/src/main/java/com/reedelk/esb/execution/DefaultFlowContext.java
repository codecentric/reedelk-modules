package com.reedelk.esb.execution;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.type.TypedContent;

import java.util.HashMap;

public class DefaultFlowContext extends HashMap<String, TypedContent<?>> implements FlowContext {

    @Override
    public void setVariable(String variableName, TypedContent<?> variableValue) {
        put(variableName, variableValue);
    }

    @Override
    public TypedContent<?> getVariable(String variableName) {
        return get(variableName);
    }
}
