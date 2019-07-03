package com.esb.services;

import com.esb.api.service.ConfigurationService;
import com.esb.api.service.ScriptEngineService;
import com.esb.module.ModulesManager;
import com.esb.services.configuration.ESBConfigurationService;
import com.esb.services.hotswap.ESBHotSwapService;
import com.esb.services.hotswap.HotSwapListener;
import com.esb.services.module.ESBModuleService;
import com.esb.services.module.EventListener;
import com.esb.services.scriptengine.ESBJavascriptEngine;
import com.esb.system.api.HotSwapService;
import com.esb.system.api.ModuleService;
import com.esb.system.api.SystemProperty;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class ESBServicesManager {

    private static final Dictionary<String, ?> NO_PROPERTIES = new Hashtable<>();

    private final ConfigurationAdmin configurationAdmin;
    private final HotSwapListener hotSwapListener;
    private final ModulesManager modulesManager;
    private final SystemProperty systemProperty;
    private final EventListener eventListener;

    private List<ServiceRegistration<?>> registeredServices = new ArrayList<>();

    public ESBServicesManager(EventListener eventListener,
                              HotSwapListener hotSwapListener,
                              ModulesManager modulesManager,
                              SystemProperty systemProperty,
                              ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
        this.hotSwapListener = hotSwapListener;
        this.systemProperty = systemProperty;
        this.modulesManager = modulesManager;
        this.eventListener = eventListener;
    }

    public void registerServices(BundleContext context) {
        registerModuleService(context);
        registerHotSwapService(context);
        registerConfigurationService(context);
        registerScriptEngineService(context);
    }

    public void unregisterServices() {
        registeredServices.forEach(ServiceRegistration::unregister);
    }

    private void registerHotSwapService(BundleContext context) {
        ESBHotSwapService service = new ESBHotSwapService(context, hotSwapListener);
        ServiceRegistration<HotSwapService> registration =
                context.registerService(HotSwapService.class, service, NO_PROPERTIES);
        registeredServices.add(registration);
    }

    private void registerModuleService(BundleContext context) {
        ESBModuleService service = new ESBModuleService(context, modulesManager, eventListener);
        ServiceRegistration<ModuleService> registration =
                context.registerService(ModuleService.class, service, NO_PROPERTIES);
        registeredServices.add(registration);
    }

    private void registerConfigurationService(BundleContext context) {
        ESBConfigurationService service = new ESBConfigurationService(configurationAdmin, systemProperty);
        service.initialize();
        ServiceRegistration<ConfigurationService> registration =
                context.registerService(ConfigurationService.class, service, NO_PROPERTIES);
        registeredServices.add(registration);
    }

    private void registerScriptEngineService(BundleContext context) {
        ESBJavascriptEngine service = ESBJavascriptEngine.INSTANCE;
        ServiceRegistration<ScriptEngineService> registration =
                context.registerService(ScriptEngineService.class, service, NO_PROPERTIES);
        registeredServices.add(registration);
    }
}
