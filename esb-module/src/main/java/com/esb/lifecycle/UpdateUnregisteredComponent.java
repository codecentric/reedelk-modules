package com.esb.lifecycle;

import com.esb.module.Module;
import com.esb.module.ModuleState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static com.esb.commons.Preconditions.checkState;
import static com.esb.module.ModuleState.*;

public class UpdateUnregisteredComponent extends AbstractStep<Module, Module> {

    private final String componentName;

    public UpdateUnregisteredComponent(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public Module run(Module module) {

        ModuleState state = module.state();

        // State == ERROR might occur when a module transition from RESOLVED to ERROR
        // for instance when an exception is thrown during onStart.
        // In that case the module transition to UNRESOLVED.
        checkState(state == UNRESOLVED || state == RESOLVED || state == ERROR,
                "State can only be UNRESOLVED, RESOLVED or ERROR");

        Collection<String> resolvedComponents = new ArrayList<>(module.resolvedComponents());
        resolvedComponents.remove(componentName);

        Collection<String> unresolvedComponents = new HashSet<>();
        if (state == UNRESOLVED) {
            unresolvedComponents.addAll(module.unresolvedComponents());
        }
        unresolvedComponents.add(componentName);

        module.unresolve(unresolvedComponents, resolvedComponents);
        return module;
    }
}
