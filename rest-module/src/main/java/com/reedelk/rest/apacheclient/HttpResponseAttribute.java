package com.reedelk.rest.apacheclient;

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
