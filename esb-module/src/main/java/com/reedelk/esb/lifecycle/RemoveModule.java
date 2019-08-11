package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.Module;

public class RemoveModule extends AbstractStep<Module, Void> {

    @Override
    public Void run(Module module) {
        modulesManager().removeModuleById(module.id());
        return NOTHING;
    }
}
