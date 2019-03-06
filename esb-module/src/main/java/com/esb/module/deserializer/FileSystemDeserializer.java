package com.esb.module.deserializer;

import com.esb.commons.FileUtils;
import com.esb.commons.FunctionWrapper;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.esb.commons.FileExtension.JSON;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;

public class FileSystemDeserializer extends AbstractDeserializer {

    private final String resourcesRootDirectory;

    public FileSystemDeserializer(String resourcesRootDirectory) {
        this.resourcesRootDirectory = resourcesRootDirectory;
    }

    private static final FileFilter JSON_FILE_FILTER = pathname ->
            pathname.isFile() &&
                    FileUtils.getExtension(pathname.getPath()).equalsIgnoreCase(JSON.value());

    @Override
    protected List<URL> getResources(String directory) {
        File resourceDirectory = Paths.get(resourcesRootDirectory, directory).toFile();
        File[] files = resourceDirectory.listFiles(JSON_FILE_FILTER);
        return files == null ?
                emptyList() :
                stream(files)
                        .map(FunctionWrapper.unchecked(file -> file.toURI().toURL()))
                        .collect(Collectors.toList());
    }
}
