package com.reedelk.rest.configuration.client;

import com.reedelk.runtime.api.annotation.DisplayName;

public enum ProxyAuthentication {
    @DisplayName("None")
    NONE,
    @DisplayName("User and password")
    USER_AND_PASSWORD
}
