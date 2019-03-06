package com.esb.lifecycle;

import com.esb.module.ModulesManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.esb.commons.Preconditions.checkArgument;
import static com.esb.commons.Preconditions.checkState;
import static java.lang.String.valueOf;

public class StepRunner {

    private List<Step<?, ?>> steps = new ArrayList<>();

    private final BundleContext context;
    private final Optional<ModulesManager> optionalModulesManager;

    private StepRunner(BundleContext context, ModulesManager modulesManager) {
        this.context = context;
        this.optionalModulesManager = Optional.ofNullable(modulesManager);
    }

    public static StepRunner get(BundleContext context, ModulesManager modulesManager) {
        return new StepRunner(context, modulesManager);
    }

    public static StepRunner get(BundleContext context) {
        return new StepRunner(context, null);
    }

    public StepRunner next(Step stepToAdd) {
        checkArgument(stepToAdd != null, "added step was null.");
        this.steps.add(stepToAdd);
        return this;
    }

    @SuppressWarnings("unchecked")
    public void execute(long moduleId) {
        // When we install a module, we don't have the Module
        // already registered in the manager, therefore the module
        // manager is not needed (e.g ResolveModuleDependencies).
        // In all the other cases, by default, the first step must
        // accept in input the module to be processed.
        Object output = null;
        if (optionalModulesManager.isPresent()) {
            output = optionalModulesManager.get().getModuleById(moduleId);
            checkState(output != null,
                    "Modules Manager was expected to contain module with id=[%s]",
                    valueOf(moduleId));
        }

        Bundle bundle = context.getBundle(moduleId);
        for (Step step : steps) {
            step.bundle(bundle);
            output = step.run(output);
        }
    }
}
