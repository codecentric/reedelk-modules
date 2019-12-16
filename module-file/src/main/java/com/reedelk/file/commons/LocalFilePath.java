package com.reedelk.file.commons;

import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

public class LocalFilePath {

    /**
     * Returns the local file path by prefixing the file path with the given base path.
     * IMPORTANT: note that this path is NOT a system (e.g Unix/Windows) dependent path,
     * but it is a path pointing to a file within the given bundle /resources directory.
     * Hence, the correct way to specify directories and subdirectories leading to a
     * wanted file is using '/' slashes and NOT '\' slashes.
     */
    public static String from(String basePath, String filePath) {
        return isBlank(basePath) ? filePath : basePath + filePath;
    }
}
