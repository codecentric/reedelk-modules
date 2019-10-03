package com.reedelk.esb.commons;

public class FunctionName {

    private static final String FUNCTION_NAME_TEMPLATE = "fun_%s";

    public static String from(String uuid) {
        return String.format(FUNCTION_NAME_TEMPLATE, uuid);
    }
}
