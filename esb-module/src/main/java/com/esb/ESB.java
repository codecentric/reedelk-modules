package com.esb;

import com.esb.api.service.ConfigurationService;
import com.esb.component.ComponentRegistry;
import com.esb.component.ESBComponent;
import com.esb.internal.api.HotSwapService;
import com.esb.internal.api.ModuleService;
import com.esb.internal.api.SystemProperty;
import com.esb.lifecycle.*;
import com.esb.module.ModulesManager;
import com.esb.services.configuration.ESBConfigurationService;
import com.esb.services.hotswap.ESBHotSwapService;
import com.esb.services.hotswap.HotSwapListener;
import com.esb.services.module.ESBEventService;
import com.esb.services.module.ESBModuleService;
import com.esb.services.module.EventListener;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Dictionary;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = ESB.class, scope = SINGLETON, immediate = true)
public class ESB implements EventListener, HotSwapListener {

    private static final Dictionary<String, ?> NO_PROPERTIES = null;

    @Reference
    public SystemProperty systemProperty;
    @Reference
    public ConfigurationAdmin configurationAdmin;

    protected BundleContext context;
    protected ModulesManager modulesManager;

    private ESBEventService eventDispatcher;
    private ComponentRegistry componentRegistry;

    @Activate
    public void start(BundleContext context) {
        this.context = context;

        modulesManager = new ModulesManager();
        componentRegistry = new ComponentRegistry(ESBComponent.allNames());
        eventDispatcher = new ESBEventService(this);

        context.addBundleListener(eventDispatcher);
        context.addServiceListener(eventDispatcher);

        registerModuleService(context);
        registerHotSwapService(context);
        registerConfigurationService(context);
    }

    @Deactivate
    public void stop(BundleContext context) {
        context.removeBundleListener(eventDispatcher);
        context.removeServiceListener(eventDispatcher);
    }

    @Override
    public synchronized void moduleStarted(long moduleId) {
        StepRunner.get(context)
                .next(new BuildAndAddModule(modulesManager))
                .next(new ResolveModuleDependencies(componentRegistry))
                .next(new BuildModule(modulesManager))
                .next(new StartModule())
                .execute(moduleId);
    }

    @Override
    public synchronized void moduleStopping(long moduleId) {
        StepRunner.get(context, modulesManager)
                .next(new StopModuleAndReleaseReferences())
                .next(new RemoveModule(modulesManager))
                .execute(moduleId);
    }

    @Override
    public synchronized void moduleStopped(long moduleId) {
        // When the OSGi container process is stopped, 'moduleStopping' is not
        // called therefore the module is still registered in the ModuleManager.
        // 'moduleStopped' is called when the OSGi container shuts down (skipping the call to moduleStopping).
        // NOTE: when a module is stopped there is no more context (Bundle Context) associated with it.
        if (modulesManager.isModuleRegistered(moduleId)) {
            StepRunner.get(context, modulesManager)
                    .next(new StopModuleAndReleaseReferences())
                    .next(new RemoveModule(modulesManager))
                    .execute(moduleId);
        }
    }

    @Override
    public synchronized void componentRegistered(String componentName) {
        componentRegistry.registerComponent(componentName);

        modulesManager.findUnresolvedModules()
                .forEach(unresolvedModule ->
                        StepRunner.get(context, modulesManager)
                                .next(new UpdateRegisteredComponent(componentName))
                                .next(new BuildModule(modulesManager))
                                .next(new StartModule())
                                .execute(unresolvedModule.id()));
    }

    @Override
    public synchronized void componentUnregistering(String componentName) {
        componentRegistry.unregisterComponent(componentName);

        modulesManager.findModulesUsingComponent(componentName)
                .forEach(moduleUsingComponent ->
                        StepRunner.get(context, modulesManager)
                                .next(new StopModuleAndReleaseReferences())
                                .next(new UpdateUnregisteredComponent(componentName))
                                .execute(moduleUsingComponent.id()));
    }

    @Override
    public synchronized void hotSwap(long moduleId, String resourcesRootDirectory) {
        StepRunner.get(context, modulesManager)
                .next(new StopModuleAndReleaseReferences())
                .next(new RemoveModule(modulesManager))
                .next(new BuildHotSwapAndAddModule(modulesManager, resourcesRootDirectory))
                .next(new ResolveModuleDependencies(componentRegistry))
                .next(new BuildModule(modulesManager))
                .next(new StartModule())
                .execute(moduleId);
    }

    private void registerHotSwapService(BundleContext context) {
        ESBHotSwapService service = new ESBHotSwapService(context, this);
        context.registerService(HotSwapService.class, service, NO_PROPERTIES);
    }

    private void registerModuleService(BundleContext context) {
        ESBModuleService service = new ESBModuleService(context, modulesManager, this);
        context.registerService(ModuleService.class, service, NO_PROPERTIES);
    }

    private void registerConfigurationService(BundleContext context) {
        ESBConfigurationService service = new ESBConfigurationService(configurationAdmin, systemProperty);
        service.initialize();
        context.registerService(ConfigurationService.class, service, NO_PROPERTIES);
    }
}
