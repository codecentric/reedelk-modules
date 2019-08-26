package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.DisplayName;

@Default("NONE")
public enum Proxy {
    @DisplayName("None")
    NONE,
    @DisplayName("Proxy")
    PROXY
}
