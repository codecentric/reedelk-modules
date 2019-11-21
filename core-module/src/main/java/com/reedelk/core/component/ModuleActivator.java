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

    @Activate
    public void start() {
        CoreJavascriptFunctions coreJavascriptFunctions = new CoreJavascriptFunctions();
        scriptEngine.register(coreJavascriptFunctions);
    }

    @Deactivate
    public void stop() {
    }

    class CoreJavascriptFunctions implements ScriptSource {

        @Override
        public Map<String, Object> bindings() {
            Map<String,Object> bindings = new HashMap<>();
            bindings.put("logger", new ScriptLogger());
            return bindings;
        }

        @Override
        public Collection<String> names() {
            return Arrays.asList("Util", "Log");
        }

        @Override
        public String resource() {
            return "/function/core-javascript-functions.js";
        }
    }
}
