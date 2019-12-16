package com.reedelk.core.component.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptLogger {

    private static final Logger logger = LoggerFactory.getLogger(ScriptLogger.class);

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void trace(String message) {
        logger.trace(message);
    }

    public void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }
}
