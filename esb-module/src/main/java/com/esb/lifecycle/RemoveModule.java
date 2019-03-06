package com.esb.lifecycle;

import com.esb.module.Module;

public class RemoveModule extends AbstractStep<Module, Void> {

    @Override
    public Void run(Module module) {
        modulesManager().removeModuleById(module.id());
        return NOTHING;
    }
}
