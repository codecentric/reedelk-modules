package com.esb.lifecycle;

import com.esb.module.Module;
import com.esb.module.ModulesManager;

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
