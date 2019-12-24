package com.reedelk.scheduler.commons;

public class Messages {

    private Messages() {
    }

    private static String formatMessage(String template, Object ...args) {
        return String.format(template, args);
    }

    interface FormattedMessage {
        String format(Object ...args);
    }

    public enum Scheduler implements FormattedMessage {
        ;

        @Override
        public String format(Object... args) {
            return null;
        }
    }
}
