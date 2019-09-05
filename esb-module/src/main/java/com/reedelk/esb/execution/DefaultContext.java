package com.reedelk.esb.execution;

import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.type.TypedContent;

import java.util.HashMap;

public class DefaultContext extends HashMap<String, TypedContent<?>> implements Context {

    @Override
    public void setVariable(String variableName, TypedContent<?> variableValue) {
        put(variableName, variableValue);
    }

    @Override
    public TypedContent<?> getVariable(String variableName) {
        return get(variableName);
    }
}
