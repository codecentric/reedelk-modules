package com.reedelk.rest.server;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static java.util.Objects.requireNonNull;

class UriTemplate {

    private final UriTemplateStructure uriTemplateStructure;

    UriTemplate(String uriTemplate) {
        requireNonNull(uriTemplate, "Uri Template");
        String uriTemplateWithoutQueryParams = filterQueryParams(uriTemplate);
        this.uriTemplateStructure = UriTemplateStructure.from(uriTemplateWithoutQueryParams);
    }

    boolean matches(String uri) {
        if (uri == null) return false;
        String uriWithoutQueryParams = filterQueryParams(uri);
        Matcher matcher = uriTemplateStructure.getPattern().matcher(uriWithoutQueryParams);
        return matcher.matches();
    }

    Map<String,String> bind(String uri) {
        requireNonNull(uri, "uri");

        List<String> variableNames = uriTemplateStructure.getVariableNames();
        Map<String, String> result = new HashMap<>();
        Matcher matcher = uriTemplateStructure.getPattern().matcher(uri);
        if (matcher.find()) {
            // We start from the first group count (the first one is the whole string)
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String name = variableNames.get(i - 1);
                String value = matcher.group(i);
                result.put(name, value);
            }
        }
        return result;
    }

    private static String filterQueryParams(String uri) {
        int hasQuery = uri.lastIndexOf("?");
        if (hasQuery != -1) {
            return uri.substring(0, hasQuery);
        } else {
            return uri;
        }
    }
}