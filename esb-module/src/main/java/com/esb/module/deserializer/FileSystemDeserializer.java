package com.esb.module.deserializer;

import com.esb.api.exception.ESBException;
import com.esb.commons.FunctionWrapper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class FileSystemDeserializer extends AbstractDeserializer {

    private final String resourcesRootDirectory;

    public FileSystemDeserializer(String resourcesRootDirectory) {
        this.resourcesRootDirectory = resourcesRootDirectory;
    }

    @Override
    protected List<URL> getResources(String directory) {
        Path targetPath = Paths.get(resourcesRootDirectory, directory);
        try {
            return Files.walk(targetPath)
                    .filter(Files::isRegularFile)
                    .map(FunctionWrapper.unchecked(path -> path.toFile().toURI().toURL()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ESBException(format("Error reading files from resource folder [%s]", targetPath), e);
        }
    }

}
