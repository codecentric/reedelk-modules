package com.reedelk.rest.configuration.listener;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.DisplayName;

@Default("CERTIFICATE_AND_PRIVATE_KEY")
public enum ServerSecurityType {

    @DisplayName("Certificate and private key")
    CERTIFICATE_AND_PRIVATE_KEY,
    @DisplayName("Key store")
    KEY_STORE
}
