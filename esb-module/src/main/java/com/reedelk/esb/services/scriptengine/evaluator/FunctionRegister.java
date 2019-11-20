package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.ScriptSource;

import java.io.IOException;
import java.io.Reader;

public class FunctionRegister extends ScriptEngineServiceAdapter {

    private final ScriptEngineProvider provider;

    public FunctionRegister(ScriptEngineProvider provider) {
        this.provider = provider;
    }

    @Override
    public void registerFunction(ScriptSource scriptSource) {
        try (Reader reader = scriptSource.get()) {
            provider.eval(scriptSource.name(), reader, scriptSource.bindings());
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }
}
