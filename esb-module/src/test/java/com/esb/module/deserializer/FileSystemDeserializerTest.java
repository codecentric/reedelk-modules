package com.esb.module.deserializer;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// TODO: Implement me
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

    private String getTmpDir() {
        Path path = Paths.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        path.toFile().mkdirs();
        return path.toString();

    }
}
