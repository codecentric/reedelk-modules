package com.esb.module.deserializer;

import java.net.URL;
import java.util.List;

public class FileSystemDeserializer extends AbstractDeserializer {

    private final String resourcesRootDirectory;

    public FileSystemDeserializer(String resourcesRootDirectory) {
        this.resourcesRootDirectory = resourcesRootDirectory;
    }

    @Override
    protected List<URL> getResources(String directory) {

        return null;
    }
}
