package com.reedelk.core.component;

import com.reedelk.core.component.logger.ScriptLogger;
import com.reedelk.runtime.api.script.ScriptSource;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = CoreModuleActivator.class, scope = SINGLETON, immediate = true)
public class CoreModuleActivator {

    @Reference
    private ScriptEngineService scriptEngineService;

    @Activate
    public void start() {
      //  UtilModule utilModule = new UtilModule();
      //  LogModule logModule = new LogModule();
      //  scriptEngineService.registerFunction(utilModule);
      //  scriptEngineService.registerFunction(logModule);
    }

    @Deactivate
    public void stop() {
    }

    class UtilModule implements ScriptSource {

        @Override
        public String name() {
            return "Util";
        }

        @Override
        public String resource() {
            return "/function/util.js";
        }
    }

    class LogModule implements ScriptSource {

        @Override
        public Map<String, Object> bindings() {
            Map<String,Object> bindings = new HashMap<>();
            bindings.put("logger", new ScriptLogger());
            return bindings;
        }

        @Override
        public String name() {
            return "Log";
        }

        @Override
        public String resource() {
            return "/function/log.js";
        }
    }
}
