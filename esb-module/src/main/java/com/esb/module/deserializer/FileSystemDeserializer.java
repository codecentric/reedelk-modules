package com.esb.module.deserializer;

import com.esb.module.DeserializedModule;
import com.esb.module.ModuleDeserializer;

public class FileSystemDeserializer implements ModuleDeserializer {

    private final String rootPath;

    public FileSystemDeserializer(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public DeserializedModule deserialize() {
        return null;
    }
}
