package com.esb.lifecycle;

import com.esb.module.Module;
import com.esb.module.ModulesManager;
import com.esb.module.deserializer.FileSystemDeserializer;
import org.osgi.framework.Bundle;

public class BuildHotSwapAndAddModule extends AbstractStep<Void, Module> {

    private final ModulesManager modulesManager;
    private final String resourcesRootDirectory;

    public BuildHotSwapAndAddModule(ModulesManager modulesManager, String resourcesRootDirectory) {
        this.modulesManager = modulesManager;
        this.resourcesRootDirectory = resourcesRootDirectory;
    }

    @Override
    public Module run(Void nothing) {
        final Bundle bundle = bundle();

        Module module = Module.builder()
                .moduleId(bundle.getBundleId())
                .name(bundle.getSymbolicName())
                .moduleFilePath(bundle.getLocation())
                .version(bundle.getVersion().toString())
                .deserializer(new FileSystemDeserializer(resourcesRootDirectory))
                .build();

        // The state of the Module just created is INSTALLED.
        modulesManager.add(module);

        return null;
    }
}
