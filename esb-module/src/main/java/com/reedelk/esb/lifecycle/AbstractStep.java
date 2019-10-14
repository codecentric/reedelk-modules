package com.reedelk.esb.lifecycle;

import com.reedelk.esb.component.ComponentRegistry;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.service.ConfigurationService;
import org.osgi.framework.Bundle;

public abstract class AbstractStep<I, O> implements Step<I, O> {

    static final Void NOTHING = null;

    private Bundle bundle;
    private ModulesManager modulesManager;
    private ComponentRegistry componentRegistry;
    private ConfigurationService configurationService;

    @Override
    public Bundle bundle() {
        return bundle;
    }

    @Override
    public void bundle(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public ModulesManager modulesManager() {
        return modulesManager;
    }

    @Override
    public void modulesManager(ModulesManager modulesManager) {
        this.modulesManager = modulesManager;
    }

    @Override
    public ComponentRegistry componentRegistry() {
        return componentRegistry;
    }

    @Override
    public void componentRegistry(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    @Override
    public ConfigurationService configurationService() {
        return configurationService;
    }

    @Override
    public void configurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
