package com.reedelk.esb.test.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public static URL createFile(String baseDir, String fileName, String content) throws IOException {
        Path baseDirPath = Paths.get(baseDir);
        baseDirPath.toFile().mkdirs();
        Path filePath = Paths.get(baseDir, fileName);
        try (FileOutputStream os = new FileOutputStream(filePath.toFile())) {
            os.write(content.getBytes());
            os.flush();
        }
        return filePath.toFile().toURI().toURL();
    }
}
