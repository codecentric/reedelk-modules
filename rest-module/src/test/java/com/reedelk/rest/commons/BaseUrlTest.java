package com.reedelk.rest.commons;

import com.reedelk.rest.configuration.HttpProtocol;
import com.reedelk.rest.configuration.RestClientConfiguration;
import org.junit.jupiter.api.Test;

class BaseUrlTest {

    @Test
    void shouldDoSomething() {
        // Given
        RestClientConfiguration configuration = new RestClientConfiguration();
        configuration.setProtocol(HttpProtocol.HTTPS);
        configuration.setHost("www.reedelk.com");
        configuration.setBasePath("/v3");


        // When
        //BaseUrl.from()

        // Then
    }

}