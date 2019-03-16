package com.esb.lifecycle;

import com.esb.module.Module;
import org.osgi.framework.Bundle;

import static com.esb.commons.Preconditions.checkState;

public class CheckModuleNotNull extends AbstractStep<Module, Module> {

    @Override
    public Module run(Module module) {
        Bundle bundle = bundle();
        long moduleId = bundle.getBundleId();
        checkState(module != null,
                "Module with id=[%d] was not found in Module Manager", moduleId);
        return module;
    }
}
