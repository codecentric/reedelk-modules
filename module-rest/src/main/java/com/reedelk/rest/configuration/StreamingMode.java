package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.DisplayName;

public enum StreamingMode {
    @DisplayName("None")
    NONE,
    @DisplayName("Always")
    ALWAYS,
    @DisplayName("Auto")
    AUTO
}
