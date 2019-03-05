package com.esb.lifecycle;

import com.esb.module.Module;

import java.util.ArrayList;
import java.util.Collection;

import static com.esb.commons.Preconditions.checkState;
import static com.esb.module.ModuleState.UNRESOLVED;
import static java.lang.String.format;

public class UpdateRegisteredComponent extends AbstractStep<Module, Module> {

    private final String componentName;

    public UpdateRegisteredComponent(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public Module run(Module module) {

        checkState(module.state() == UNRESOLVED,
                format("Module state was=%s. Only state UNRESOLVED allowed", module.state()));

        Collection<String> resolvedComponents = new ArrayList<>(module.resolvedComponents());
        Collection<String> unresolvedComponents = new ArrayList<>(module.unresolvedComponents());

        if (unresolvedComponents.contains(componentName)) {
            resolvedComponents.add(componentName);
            unresolvedComponents.remove(componentName);
        }

        if (unresolvedComponents.isEmpty()) {
            module.resolve(resolvedComponents);
        } else {
            module.unresolve(unresolvedComponents, resolvedComponents);
        }

        return module;
    }
}
