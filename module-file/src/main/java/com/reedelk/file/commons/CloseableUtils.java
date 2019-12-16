package com.reedelk.file.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class CloseableUtils {

    private static final Logger logger = LoggerFactory.getLogger(CloseableUtils.class);

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.warn("Could not close", e);
                // nothing we can do here
            }
        }
    }
}
