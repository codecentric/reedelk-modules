package com.reedelk.rest.client.uri;

import com.reedelk.runtime.api.commons.StringUtils;

import java.util.Map;

public class EmptyURIPathComponent implements URIPathComponent {
    @Override
    public String expand(Map<String, String> pathParams, Map<String, String> queryParams) {
        return StringUtils.EMPTY;
    }
}
