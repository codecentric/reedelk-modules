package com.reedelk.core.component;

import com.reedelk.core.component.logger.ScriptLogger;
import com.reedelk.runtime.api.script.ScriptSource;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = ModuleActivator.class, scope = SINGLETON, immediate = true)
public class ModuleActivator {

    @Reference
    private ScriptEngineService scriptEngine;

    private final CoreScriptModules coreScriptModules = new CoreScriptModules();

    @Activate
    public void start() {
        scriptEngine.register(coreScriptModules);
    }

    @Deactivate
    public void stop() {
        scriptEngine.unregister(coreScriptModules);
    }

    class CoreScriptModules implements ScriptSource {

        @Override
        public Map<String, Object> bindings() {
            Map<String,Object> bindings = new HashMap<>();
            bindings.put("logger", new ScriptLogger());
            return bindings;
        }

        @Override
        public Collection<String> scriptModuleNames() {
            return Arrays.asList("Util", "Log");
        }

        @Override
        public String resource() {
            return "/function/core-javascript-functions.js";
        }
    }
}
