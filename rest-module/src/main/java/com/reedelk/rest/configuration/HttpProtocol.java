package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.Default;

@Default("HTTP")
public enum HttpProtocol {
    HTTP,
    HTTPS
}
