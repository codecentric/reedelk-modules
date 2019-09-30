package com.reedelk.esb.module.deserializer;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.commons.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static com.reedelk.esb.commons.FunctionWrapper.unchecked;
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
        try (Stream<Path> walk = Files.walk(targetPath)) {
            return walk
                    .filter(path -> path.toFile().isFile())
                    .filter(path -> FileUtils.hasExtension(path, suffix))
                    .map(unchecked(path -> path.toFile().toURI().toURL()))
                    .collect(toList());
        } catch (IOException e) {
            throw new ESBException(format("Error reading files from resource folder [%s]", targetPath), e);
        }
    }
}
