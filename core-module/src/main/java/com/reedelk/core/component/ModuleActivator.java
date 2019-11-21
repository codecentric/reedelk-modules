package com.reedelk.core.component;

import com.reedelk.core.component.logger.ScriptLogger;
import com.reedelk.runtime.api.script.ScriptSource;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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

    @Activate
    public void start(BundleContext context) {
        CoreScriptModules coreScriptModules = new CoreScriptModules(context.getBundle().getBundleId());
        scriptEngine.register(coreScriptModules);
    }

    class CoreScriptModules implements ScriptSource {

        private final long moduleId;

        CoreScriptModules(long moduleId) {
            this.moduleId = moduleId;
        }

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
        public long moduleId() {
            return moduleId;
        }

        @Override
        public String resource() {
            return "/function/core-javascript-functions.js";
        }
    }
}
