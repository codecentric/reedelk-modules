package com.esb.lifecycle;

import com.esb.component.ComponentRegistry;
import com.esb.module.ModulesManager;
import org.osgi.framework.Bundle;

public abstract class AbstractStep<I, O> implements Step<I, O> {

    protected static final Void NOTHING = null;

    private Bundle bundle;
    private ModulesManager modulesManager;
    private ComponentRegistry componentRegistry;

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
}
