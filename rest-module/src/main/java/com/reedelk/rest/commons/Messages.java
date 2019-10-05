package com.reedelk.rest.commons;

public class Messages {

    private Messages() {
    }

    private static String formatMessage(String template, Object ...args) {
        return String.format(template, args);
    }

    interface FormattedMessage {
        String format(Object ...args);
    }

    public enum RestClient implements FormattedMessage {

        REQUEST_FAILED("Failed to connect to %s: %s"),
        REQUEST_CANCELLED("Failed to connect to %s: request has been cancelled");

        private String msg;

        RestClient(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }
}
