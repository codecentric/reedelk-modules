package com.reedelk.rest.configuration.client;

import com.reedelk.runtime.api.annotation.DisplayName;

public enum Authentication {
    @DisplayName("None")
    NONE,
    @DisplayName("Basic")
    BASIC,
    @DisplayName("Digest")
    DIGEST
}
