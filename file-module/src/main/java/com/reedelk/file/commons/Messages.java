package com.reedelk.file.commons;

public class Messages {

    private Messages() {
    }

    private static String formatMessage(String template, Object ...args) {
        return String.format(template, args);
    }

    interface FormattedMessage {
        String format(Object ...args);
    }

    public enum FileReadComponent implements FormattedMessage {

        FILE_NOT_FOUND("Could not find file with name[%s], base path=[%s]");

        private String msg;

        FileReadComponent(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum ModuleFileReadComponent implements FormattedMessage {

        FILE_NOT_FOUND("Could not find file with name[%s], base path=[%s] in module with id=[%d]");

        private String msg;

        ModuleFileReadComponent(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum FileWriteComponent implements FormattedMessage {

        ERROR_CREATING_DIRECTORIES("Could not create directories for file path=[%s]: %s");

        private String msg;

        FileWriteComponent(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }

    }
}
