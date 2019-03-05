package com.esb.lifecycle;

import com.esb.flow.ModulesManager;
import com.esb.module.Module;

public class RemoveModule extends AbstractStep<Module, Void> {

    private final ModulesManager modulesManager;

    public RemoveModule(ModulesManager modulesManager) {
        this.modulesManager = modulesManager;
    }

    @Override
    public Void run(Module module) {
        modulesManager.removeModuleById(module.id());
        return NOTHING;
    }
}
