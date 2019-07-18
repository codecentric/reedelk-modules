package com.esb.services.scriptengine;

import com.esb.api.service.ScriptExecutionResult;

import javax.script.Bindings;

public class DefaultScriptExecutionResult implements ScriptExecutionResult {

    private final Object object;
    private final Bindings bindings;

    public DefaultScriptExecutionResult(Object object, Bindings bindings) {
        this.object = object;
        this.bindings = bindings;
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public Bindings getBindings() {
        return bindings;
    }
}
