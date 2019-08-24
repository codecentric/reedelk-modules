package com.reedelk.rest.commons;

public class RemoveQueryParams {

    public static String from(String uri) {
        int hasQuery = uri.lastIndexOf("?");
        if (hasQuery != -1) {
            return uri.substring(0, hasQuery);
        } else {
            return uri;
        }
    }
}
