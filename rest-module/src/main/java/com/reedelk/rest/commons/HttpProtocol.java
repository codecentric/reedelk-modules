package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;

public enum HttpProtocol {
    HTTP,
    HTTPS;

    public static HttpProtocol from(String protocol) {
        return StringUtils.isBlank(protocol) ?
                HttpProtocol.HTTP :
                HttpProtocol.valueOf(protocol.toUpperCase());
    }
}
