package com.esb.module.deserializer;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FileSystemDeserializerTest {

    private String tmpDir;

    @BeforeEach
    void setUp() {
        tmpDir = getTmpDir();
    }

    @AfterEach
    void tearDown() {
        new File(tmpDir).delete();
    }

    @Test
    void shouldRecursivelyReturnAllFiles() throws IOException {
        // Given
        Path somethingDir = Paths.get(tmpDir, "something");
        somethingDir.toFile().mkdirs();

        Path nestedDirectory = Paths.get(somethingDir.toString(), "nested");
        nestedDirectory.toFile().mkdirs();

        String flow1 = createFile(somethingDir.toString(), "flow1.flow");
        String flow2 = createFile(somethingDir.toString(), "flow2.flow");
        String flow3 = createFile(somethingDir.toString(), "flow3.txt");
        String flow4 = createFile(somethingDir.toString(), "flow4.flow");
        String flow5 = createFile(nestedDirectory.toString(), "flow5.json");


        FileSystemDeserializer deserializer = new FileSystemDeserializer(tmpDir);

        // When
        List<URL> folderResources = deserializer.getResources("/something", "flow");

        // Then
        assertFound(folderResources, flow1);
        assertFound(folderResources, flow2);
        assertFound(folderResources, flow4);

        assertThat(folderResources).hasSize(3);
    }

    private void assertFound(Collection<URL> folderResources, String resourcePath) {
        assertThat(folderResources
                .stream()
                .anyMatch(url -> url.getFile().equals(resourcePath)))
                .withFailMessage("Path [%s] not found in resources", resourcePath)
                .isTrue();

    }

    private String createFile(String baseDir, String fileName) throws IOException {
        File file = Paths.get(baseDir, fileName).toFile();
        try (FileOutputStream os = new FileOutputStream(file)) {
            os.write("{}".getBytes());
        }
        return file.toString();
    }

    private String getTmpDir() {
        Path path = Paths.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        path.toFile().mkdirs();
        return path.toString();

    }
}
