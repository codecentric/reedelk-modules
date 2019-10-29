package com.reedelk.esb.module.deserializer;


import com.reedelk.esb.test.utils.FileUtils;
import com.reedelk.esb.test.utils.TmpDir;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileSystemDeserializerTest {

    private static final String EMTPY_FLOW_CONTENT = "{}";

    private String tmpDir;

    @BeforeEach
    void setUp() {
        tmpDir = TmpDir.get();
    }

    @AfterEach
    void tearDown() {
        new File(tmpDir).delete();
    }

    @Test
    void shouldRecursivelyReturnAllFiles() throws IOException {
        // Given
        Path somethingDir = Paths.get(tmpDir, "something");
        Path nestedDirectory = Paths.get(somethingDir.toString(), "nested");

        URL flow1 = FileUtils.createFile(somethingDir.toString(), "flow1.flow", EMTPY_FLOW_CONTENT);
        URL flow2 = FileUtils.createFile(somethingDir.toString(), "flow2.flow", EMTPY_FLOW_CONTENT);
        URL flow3 = FileUtils.createFile(somethingDir.toString(), "flow3.txt", EMTPY_FLOW_CONTENT);
        URL flow4 = FileUtils.createFile(somethingDir.toString(), "flow4.flow", EMTPY_FLOW_CONTENT);
        URL flow5 = FileUtils.createFile(nestedDirectory.toString(), "flow5.json", EMTPY_FLOW_CONTENT);


        FileSystemDeserializer deserializer = new FileSystemDeserializer(tmpDir);

        // When
        List<URL> folderResources = deserializer.getResources("something", "flow");

        // Then
        assertFound(folderResources, flow1);
        assertFound(folderResources, flow2);
        assertFound(folderResources, flow4);

        assertThat(folderResources).hasSize(3);
    }

    private void assertFound(Collection<URL> folderResources, URL resourcePath) {
        assertThat(folderResources
                .stream()
                .anyMatch(url -> url.equals(resourcePath)))
                .withFailMessage("Path [%s] not found in resources [%s]", resourcePath, folderResources)
                .isTrue();

    }
}
