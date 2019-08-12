package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.state.ModuleState;

import static com.reedelk.esb.commons.Preconditions.checkState;

public class RemoveModule extends AbstractStep<Module, Void> {
    @Override
    public Void run(Module module) {
        checkState(module.state() != ModuleState.STARTED,
                "Module with id=[%d], name=[%s] could not be removed: its state is [%s]",
                module.id(),
                module.name(),
                module.state());
        modulesManager().removeModuleById(module.id());
        return NOTHING;
    }
}
