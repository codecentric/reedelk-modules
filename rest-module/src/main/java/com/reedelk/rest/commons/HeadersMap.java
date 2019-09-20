package com.reedelk.rest.commons;

import java.util.List;
import java.util.TreeMap;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

public class HeadersMap extends TreeMap<String, List<String>> {
    HeadersMap() {
        super(CASE_INSENSITIVE_ORDER);
    }
}
