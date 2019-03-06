package com.esb.lifecycle;

import com.esb.module.Module;
import com.esb.module.deserializer.BundleDeserializer;
import org.osgi.framework.Bundle;

public class CreateModule extends AbstractStep<Void, Module> {

    @Override
    public Module run(Void input) {
        final Bundle bundle = bundle();

        // The state of the Module just created is INSTALLED.
        return Module.builder()
                .moduleId(bundle.getBundleId())
                .name(bundle.getSymbolicName())
                .moduleFilePath(bundle.getLocation())
                .version(bundle.getVersion().toString())
                .deserializer(new BundleDeserializer(bundle))
                .build();
    }
}
