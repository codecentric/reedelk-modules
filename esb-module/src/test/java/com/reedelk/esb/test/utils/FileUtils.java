package com.reedelk.esb.test.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static URL createFile(String baseDir, String fileName, String content) throws IOException {
        Path filePath = Paths.get(baseDir, fileName);
        return createFile(filePath, content);
    }

    public static URL createFile(Path filePathAndName, String content) throws IOException {
        new File(filePathAndName.toFile().getParent()).mkdirs();
        try (FileOutputStream os = new FileOutputStream(filePathAndName.toFile())) {
            os.write(content.getBytes());
            os.flush();
        }
        return filePathAndName.toFile().toURI().toURL();
    }
}
