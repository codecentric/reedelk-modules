package com.esb.lifecycle;

import com.esb.module.Module;
import com.esb.module.deserializer.FileSystemDeserializer;
import org.osgi.framework.Bundle;

public class HotSwapModule extends AbstractStep<Void, Module> {

    private final String resourcesRootDirectory;

    public HotSwapModule(String resourcesRootDirectory) {
        this.resourcesRootDirectory = resourcesRootDirectory;
    }

    @Override
    public Module run(Void nothing) {
        final Bundle bundle = bundle();

        // The state of the Module just created is INSTALLED.
        return Module.builder()
                .moduleId(bundle.getBundleId())
                .name(bundle.getSymbolicName())
                .moduleFilePath(bundle.getLocation())
                .version(bundle.getVersion().toString())
                .deserializer(new FileSystemDeserializer(resourcesRootDirectory))
                .build();
    }
}
