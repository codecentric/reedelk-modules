package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.pubsub.Event;
import com.reedelk.esb.pubsub.OnMessage;
import com.reedelk.esb.services.scriptengine.JavascriptEngineProvider;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.ScriptSource;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.reedelk.esb.pubsub.Action.Module.ActionModuleUninstalled;
import static com.reedelk.esb.pubsub.Action.Module.Uninstalled;

public class ScriptSourceEvaluator extends ScriptEngineServiceAdapter {

    private final Map<Long, Collection<String>> MODULE_MODULE_NAMES_MAP = new HashMap<>();

    public ScriptSourceEvaluator() {
        Event.operation.subscribe(Uninstalled, this);
    }

    @Override
    public void register(ScriptSource scriptSource) {
        try (Reader reader = scriptSource.get()) {
            JavascriptEngineProvider.getInstance()
                    .eval(scriptSource.scriptModuleNames(), reader, scriptSource.bindings());
            MODULE_MODULE_NAMES_MAP.put(scriptSource.moduleId(), scriptSource.scriptModuleNames());
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    @OnMessage
    public void onModuleUninstalled(ActionModuleUninstalled action) {
        long moduleId = action.getMessage();
        if (MODULE_MODULE_NAMES_MAP.containsKey(moduleId)) {
            MODULE_MODULE_NAMES_MAP.remove(moduleId).forEach(scriptModuleName ->
                    JavascriptEngineProvider.getInstance().clear(scriptModuleName));
        }
    }
}
