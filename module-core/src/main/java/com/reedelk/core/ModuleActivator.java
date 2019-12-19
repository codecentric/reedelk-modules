package com.reedelk.core;

import com.reedelk.core.script.CoreScriptModules;
import com.reedelk.runtime.api.script.ScriptEngineService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
}
