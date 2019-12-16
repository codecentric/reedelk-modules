package com.reedelk.core.component.logger;

import com.reedelk.runtime.api.annotation.DisplayName;

public enum LoggerLevel implements MessageLogger {

    @DisplayName("Info")
    INFO {
        @Override
        public void log(Object message) {
            LoggerComponent.logger.info(asLoggableString(message));
        }
    },

    @DisplayName("Debug")
    DEBUG {
        @Override
        public void log(Object message) {
            LoggerComponent.logger.debug(asLoggableString(message));
        }
    },

    @DisplayName("Warn")
    WARN {
        @Override
        public void log(Object message) {
            LoggerComponent.logger.warn(asLoggableString(message));
        }
    },

    @DisplayName("Error")
    ERROR {
        @Override
        public void log(Object message) {
            LoggerComponent.logger.error(asLoggableString(message));
        }
    },

    @DisplayName("Trace")
    TRACE {
        @Override
        public void log(Object message) {
            LoggerComponent.logger.trace(asLoggableString(message));
        }
    };

    private static String asLoggableString(Object message) {
        return message == null ? null : message.toString();
    }
}
