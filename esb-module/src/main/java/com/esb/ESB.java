package com.esb;

import com.esb.component.ComponentRegistry;
import com.esb.component.ESBComponent;
import com.esb.lifecycle.*;
import com.esb.module.ModulesManager;
import com.esb.services.ESBServicesManager;
import com.esb.services.hotswap.HotSwapListener;
import com.esb.services.module.ESBEventService;
import com.esb.services.module.EventListener;
import com.esb.system.api.SystemProperty;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = ESB.class, scope = SINGLETON, immediate = true)
public class ESB implements EventListener, HotSwapListener {

    @Reference
    private SystemProperty systemProperty;
    @Reference
    private ConfigurationAdmin configurationAdmin;

    protected BundleContext context;
    protected ModulesManager modulesManager;

    private ESBEventService eventDispatcher;
    private ESBServicesManager servicesManager;
    private ComponentRegistry componentRegistry;

    @Activate
    public void start(BundleContext context) {
        this.context = context;

        modulesManager = new ModulesManager();
        componentRegistry = new ComponentRegistry(ESBComponent.allNames());
        eventDispatcher = new ESBEventService(this);

        context.addBundleListener(eventDispatcher);
        context.addServiceListener(eventDispatcher);

        servicesManager = new ESBServicesManager(
                ESB.this,
                ESB.this,
                modulesManager,
                systemProperty,
                configurationAdmin);
        servicesManager.registerServices(context);
    }

    @Deactivate
    public void stop(BundleContext context) {
        context.removeBundleListener(eventDispatcher);
        context.removeServiceListener(eventDispatcher);
        servicesManager.unregisterServices();
    }

    @Override
    public synchronized void moduleStarted(long moduleId) {
        StepRunner.get(context, modulesManager, componentRegistry)
                .next(new CreateModule())
                .next(new AddModule())
                .next(new ResolveModuleDependencies())
                .next(new BuildModule())
                .next(new StartModule())
                .execute(moduleId);
    }

    @Override
    public synchronized void moduleStopping(long moduleId) {
        StepRunner.get(context, modulesManager)
                .next(new CheckModuleNotNull())
                .next(new StopModuleAndReleaseReferences())
                .next(new RemoveModule())
                .execute(moduleId);
    }

    /**
     * When the OSGi container process is stopped, 'moduleStopping' is not called therefore the module is
     * still registered in the ModuleManager. 'moduleStopped' is called when the OSGi container shuts down
     * (skipping the call to moduleStopping). Note that when a module is stopped there is no more context
     * (Bundle Context) associated with it.
     */
    @Override
    public synchronized void moduleStopped(long moduleId) {
        if (modulesManager.isModuleRegistered(moduleId)) {
            StepRunner.get(context, modulesManager)
                    .next(new StopModuleAndReleaseReferences())
                    .next(new RemoveModule())
                    .execute(moduleId);
        }
    }

    @Override
    public synchronized void componentRegistered(String componentName) {
        componentRegistry.registerComponent(componentName);

        modulesManager.findUnresolvedModules()
                .forEach(unresolvedModule ->
                        StepRunner.get(context, modulesManager)
                                .next(new CheckModuleNotNull())
                                .next(new UpdateRegisteredComponent(componentName))
                                .next(new BuildModule())
                                .next(new StartModule())
                                .execute(unresolvedModule.id()));
    }

    @Override
    public synchronized void componentUnregistering(String componentName) {
        componentRegistry.unregisterComponent(componentName);

        modulesManager.findModulesUsingComponent(componentName)
                .forEach(moduleUsingComponent ->
                        StepRunner.get(context, modulesManager)
                                .next(new CheckModuleNotNull())
                                .next(new StopModuleAndReleaseReferences())
                                .next(new UpdateUnregisteredComponent(componentName))
                                .execute(moduleUsingComponent.id()));
    }

    @Override
    public synchronized void hotSwap(long moduleId, String resourcesRootDirectory) {
        StepRunner.get(context, modulesManager, componentRegistry)
                .next(new CheckModuleNotNull())
                .next(new StopModuleAndReleaseReferences())
                .next(new RemoveModule())
                .next(new HotSwapModule(resourcesRootDirectory))
                .next(new AddModule())
                .next(new ResolveModuleDependencies())
                .next(new BuildModule())
                .next(new StartModule())
                .execute(moduleId);
    }
}
