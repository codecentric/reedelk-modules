package com.reedelk.esb.commons;

public class Messages {

    private Messages() {
    }

    private static String formatMessage(String template, Object ...args) {
        return String.format(template, args);
    }

    interface FormattedMessage {
        String format(Object ...args);
    }

    public enum Flow implements FormattedMessage {
        FORCE_STOP("Error forcing stop flow with id=[%s]."),
        FORCE_STOP_WITH_TITLE("Error forcing stop flow with id=[%s] and title '%s'."),
        START("Flow with id=[%s] started."),
        START_WITH_TITLE("Flow with id=[%s] and title '%s' started."),
        START_ERROR("Error starting flow with id=[%s]."),
        START_ERROR_WITH_TITLE("Error starting flow with id=[%s] and title '%s'."),
        STOP_ERROR("Error stopping flow with id=[%s]."),
        STOP_ERROR_WITH_TITLE("Error stopping flow with id=[%s] and title '%s'."),
        BUILD_ERROR("Error building flow with id=[%s]."),
        BUILD_ERROR_WITH_TITLE("Error building flow with id=[%s] and title '%s'.");

        private String msg;

        Flow(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Module implements FormattedMessage {

        DESERIALIZATION_ERROR("Error de-serializing module with id=[%d], name=[%s], version=[%s], module file path=[%s]."),
        VALIDATION_ERROR("Error validating module with id=[%d], name=[%s], version=[%s], module file path=[%s].");

        private String msg;

        Module(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }
}
