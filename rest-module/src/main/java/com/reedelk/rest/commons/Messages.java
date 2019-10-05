package com.reedelk.rest.commons;

public class Messages {

    private Messages() {
    }

    public static String formatMessage(String template, Object ...args) {
        return String.format(template, args);
    }

    public static class RestClient {

        private RestClient() {
        }

        public static String REQUEST_FAILED = "RestClient request for URI=[%s] failed";
        public static String REQUEST_CANCELLED = "RestClient request for URI=[%s] has been cancelled";
    }
}
