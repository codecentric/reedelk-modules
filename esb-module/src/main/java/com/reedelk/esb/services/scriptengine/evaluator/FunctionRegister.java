package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.JavascriptEngineProvider;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.ScriptSource;

import java.io.IOException;
import java.io.Reader;

public class FunctionRegister extends ScriptEngineServiceAdapter {

    @Override
    public void registerFunction(ScriptSource scriptSource) {
        try (Reader reader = scriptSource.get()) {
            JavascriptEngineProvider.getInstance()
                    .eval(scriptSource.name(), reader, scriptSource.bindings());
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }
}
