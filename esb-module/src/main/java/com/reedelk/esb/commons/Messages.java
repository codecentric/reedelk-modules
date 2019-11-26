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
        FORCE_STOP("Error forcing stop flow with id=[%s]: %s"),
        FORCE_STOP_WITH_TITLE("Error forcing stop flow with id=[%s] and title '%s': %s"),
        START("Flow with id=[%s] started."),
        START_WITH_TITLE("Flow with id=[%s] and title '%s' started."),
        START_ERROR("Error starting flow with id=[%s]: %s"),
        START_ERROR_WITH_TITLE("Error starting flow with id=[%s] and title '%s': %s"),
        STOP_ERROR("Error stopping flow with id=[%s]: %s"),
        STOP_ERROR_WITH_TITLE("Error stopping flow with id=[%s] and title '%s': %s"),
        BUILD_ERROR("Error building flow with id=[%s]: %s"),
        BUILD_ERROR_WITH_TITLE("Error building flow with id=[%s] and title '%s': %s"),
        VALIDATION_ID_NOT_UNIQUE("Error validating module with name=[%s]: There are at least two flows with the same ID. Flow IDs must be unique."),
        VALIDATION_ID_NOT_VALID("Error validating module with name=[%s]: The 'id' property must be defined and not empty in any JSON flow definition."),
        EXECUTION_ERROR("an error has occurred while executing flow:" +
                "\n----------------------------------------------------------\n" +
                "- Module id=%d\n" +
                "- Module name=%s\n" +
                "- Flow id=%s\n" +
                "- Flow title=%s\n" +
                "- Error type=%s\n" +
                "- Error message:\n" +
                "%s" +
                "\n----------------------------------------------------------\n");

        private String msg;

        Flow(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Subflow implements FormattedMessage {

        VALIDATION_ID_NOT_UNIQUE("Error validating module with name=[%s]: There are at least two subflows with the same ID. Subflow IDs must be unique."),
        VALIDATION_ID_NOT_VALID("Error validating module with name=[%s]: The 'id' property must be defined and not empty in any JSON subflow definition.");

        private String msg;

        Subflow(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Config implements FormattedMessage {

        VALIDATION_ID_NOT_UNIQUE("Error validating module with name=[%s]: There are at least two configurations with the same ID. Configuration IDs must be unique."),
        VALIDATION_ID_NOT_VALID("Error validating module with name=[%s]: The 'id' property must be defined and not empty in any JSON configuration definition.");

        private String msg;

        Config(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Module implements FormattedMessage {

        DESERIALIZATION_ERROR("Error de-serializing module with id=[%d], name=[%s]: %s"),
        FILE_NOT_FOUND_ERROR("Could not find local file file=[%s] in module with id=[%d], name=[%s]."),
        FILE_FIND_IO_ERROR("An I/O occurred while reading file=[%s] in module with id=[%d], name=[%s]: %s");

        private String msg;

        Module(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Deserializer implements FormattedMessage {

        UNSUPPORTED_COLLECTION_TYPE("Error while mapping property=[%s]: not a supported collection type."),
        CONFIGURATION_NOT_FOUND("Could not find configuration with id=[%s]");

        private String msg;

        Deserializer(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum ConfigProperty implements FormattedMessage {

        NOT_FOUND_WITH_KEY_AND_PID_AND_DEFAULT("Could not find config property with key=[%s] for config pid=[%s], using defaultValue=[%s]."),
        NOT_FOUND_WITH_KEY_AND_PID("Could not find config property with key=[%s] for config pid=[%s]."),
        NOT_FOUND_WITH_KEY("Could not find config property with key=[%s]."),
        UNSUPPORTED_CONVERSION("Unsupported conversion. Could not convert config property with key=[%s] for config pid=[%s] to type=[%s].");

        private String msg;

        ConfigProperty(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }

    }

    public enum Script implements FormattedMessage {

        SCRIPT_BLOCK_COMPILATION_ERROR("Could not compile script: %s,\n- Script code:\n%s"),
        SCRIPT_SOURCE_COMPILATION_ERROR("Could not compile script source: %s, \n- Source: %s\n- Module names: %s"),
        SCRIPT_EXECUTION_ERROR("Could not execute script: %s,\n- Script code:\n%s");

        private String msg;

        Script(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Execution implements FormattedMessage {

        ERROR_FIRST_SUCCESSOR_LEADING_TO_END("Could not find first successor of component=[%s], leading to component=[%s]");

        private String msg;

        Execution(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum HotSwap implements FormattedMessage {

        MODULE_NOT_FOUND("Hot Swap failed: could not find registered module from target file path=%s");

        private String msg;

        HotSwap(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum PubSub implements FormattedMessage {

        ERROR_DELIVERING_MESSAGE("Could not deliver Service Bus Message");

        private String msg;

        PubSub(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }
}
