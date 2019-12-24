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
        REQUEST_URL_ERROR("Could not build request URL with the following parameters host=[%s], port=[%s], basePath=[%s], scheme=[%s]: %s"),
        CONFIG_CLIENT_NULL_ERROR("Expected JSON definition with 'configuration' object property OR 'baseURL' property"),
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

    public enum RestListener implements FormattedMessage {

        LISTENER_CONFIG_MISSING("RestListener 'configuration' property must not be null in the JSON definition"),
        ERROR_MULTIPART_ATTRIBUTE_VALUE("Error extracting Multipart attribute value for part named '%s'"),
        ERROR_MULTIPART_FILE_UPLOAD_VALUE("Error extracting Multipart file upload value for part named '%s'"),
        ERROR_MULTIPART_NOT_SUPPORTED("Multipart Content-Type is only supported for requests with method POST and HTTP version 1.1");

        private String msg;

        RestListener(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }
}
