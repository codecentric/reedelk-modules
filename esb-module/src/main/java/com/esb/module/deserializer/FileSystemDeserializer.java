package com.esb.module.deserializer;

import com.esb.module.DeserializedModule;
import com.esb.module.ModuleDeserializer;

public class FileSystemDeserializer implements ModuleDeserializer {

    private final String resourcesRootDirectory;

    public FileSystemDeserializer(String resourcesRootDirectory) {
        this.resourcesRootDirectory = resourcesRootDirectory;
    }

    @Override
    public DeserializedModule deserialize() {
        return null;
    }
}
