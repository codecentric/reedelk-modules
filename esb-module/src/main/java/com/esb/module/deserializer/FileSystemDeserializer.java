package com.esb.module.deserializer;

import com.esb.api.exception.ESBException;
import com.esb.internal.commons.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.esb.commons.FunctionWrapper.unchecked;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class FileSystemDeserializer extends AbstractDeserializer {

    private final String resourcesRootDirectory;

    public FileSystemDeserializer(String resourcesRootDirectory) {
        this.resourcesRootDirectory = resourcesRootDirectory;
    }

    @Override
    protected List<URL> getResources(String directory, String suffix) {
        Path targetPath = Paths.get(resourcesRootDirectory, directory);
        try {
            return Files.walk(targetPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> FileUtils.hasExtension(path, suffix))
                    .map(unchecked(path -> path.toFile().toURI().toURL()))
                    .collect(toList());
        } catch (IOException e) {
            throw new ESBException(format("Error reading files from resource folder [%s]", targetPath), e);
        }
    }

}
