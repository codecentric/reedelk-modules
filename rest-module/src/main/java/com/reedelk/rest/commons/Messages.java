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
        REQUEST_CANCELLED("Failed to connect to %s: request has been cancelled"),
        DIGEST_AUTH_MISSING("Digest Authentication Configuration must be present in the JSON definition when 'authentication' property is 'DIGEST'"),
        BASIC_AUTH_MISSING("Basic Authentication Configuration must be present in the JSON definition when 'authentication' property is 'BASIC'"),
        PROXY_CONFIG_MISSING("Proxy Configuration must be present in the JSON definition when 'proxy' property is 'PROXY'");

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
