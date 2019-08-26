package com.reedelk.rest.commons;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Predicate;

public class Predicates {

    public static final Predicate<String> IS_VALID_URL = test -> {
        try {
            new URL(test);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    };
}
