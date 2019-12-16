package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;

public class QueryParams {

    public static String of(String uri) {
        int hasQuery = uri.lastIndexOf("?");
        if (hasQuery != -1) {
            return uri.substring(hasQuery + 1);
        } else {
            return StringUtils.EMPTY;
        }
    }
}
