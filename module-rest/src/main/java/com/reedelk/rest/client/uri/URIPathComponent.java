package com.reedelk.rest.client.uri;

import java.util.Map;

public interface URIPathComponent {
    String expand(Map<String, String> pathParams, Map<String, String> queryParams);
}
