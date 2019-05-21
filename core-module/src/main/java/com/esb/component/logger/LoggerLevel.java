package com.esb.component.logger;

public enum LoggerLevel implements MessageLogger {

    INFO {
        @Override
        public void log(Object message) {
            LoggerComponent.logger.info(asLoggableString(message));
        }
    },
    DEBUG {
        @Override
        public void log(Object message) {
            LoggerComponent.logger.debug(asLoggableString(message));
        }
    },
    WARN {
        @Override
        public void log(Object message) {
            LoggerComponent.logger.warn(asLoggableString(message));
        }
    },
    ERROR {
        @Override
        public void log(Object message) {
            LoggerComponent.logger.error(asLoggableString(message));
        }
    },
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
