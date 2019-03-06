package com.esb.lifecycle;

import com.esb.module.ModulesManager;
import com.esb.module.Module;
import org.osgi.framework.Bundle;

public class BuildAndAddModule extends AbstractStep<Void, Module> {

    private final ModulesManager modulesManager;

    public BuildAndAddModule(ModulesManager modulesManager) {
        this.modulesManager = modulesManager;
    }

    @Override
    public Module run(Void input) {
        final Bundle bundle = bundle();

        Module module = Module.builder()
                .moduleId(bundle.getBundleId())
                .name(bundle.getSymbolicName())
                .moduleFilePath(bundle.getLocation())
                .version(bundle.getVersion().toString())
                .build();

        // The state of the Module just created is INSTALLED.
        modulesManager.add(module);

        return module;
    }
}
