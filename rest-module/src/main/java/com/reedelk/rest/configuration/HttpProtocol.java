package com.reedelk.rest.configuration;

import com.reedelk.rest.commons.StringUtils;
import com.reedelk.runtime.api.annotation.Default;

@Default("HTTP")
public enum HttpProtocol {
    HTTP,
    HTTPS;

    public static HttpProtocol from(String protocol) {
        return StringUtils.isBlank(protocol) ?
                HttpProtocol.HTTP :
                HttpProtocol.valueOf(protocol.toUpperCase());
    }
}
