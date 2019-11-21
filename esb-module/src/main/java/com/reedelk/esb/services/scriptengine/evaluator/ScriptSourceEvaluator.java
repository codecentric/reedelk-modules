package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.JavascriptEngineProvider;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.ScriptSource;

import java.io.IOException;
import java.io.Reader;

public class ScriptSourceEvaluator extends ScriptEngineServiceAdapter {

    @Override
    public void register(ScriptSource scriptSource) {
        try (Reader reader = scriptSource.get()) {
            JavascriptEngineProvider.getInstance()
                    .eval(scriptSource.scriptModuleNames(), reader, scriptSource.bindings());
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void unregister(ScriptSource scriptSource) {
        scriptSource.scriptModuleNames()
                .forEach(scriptModuleName ->
                        JavascriptEngineProvider.getInstance().clear(scriptModuleName));
    }
}
