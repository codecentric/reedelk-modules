package com.reedelk.rest.client.response;

public interface HttpResponseAttribute {

    static String statusCode() {
        return "statusCode";
    }

    static String reasonPhrase() {
        return "reasonPhrase";
    }

    static String headers() {
        return "headers";
    }

}
