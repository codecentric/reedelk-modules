package com.reedelk.rest.commons;

import com.reedelk.rest.configuration.HttpProtocol;
import com.reedelk.rest.configuration.RestClientConfiguration;
import com.reedelk.runtime.api.exception.ESBException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseUrlTest {

    @Test
    void shouldBuildBaseUrlCorrectly() {
        // Given
        RestClientConfiguration configuration = new RestClientConfiguration();
        configuration.setProtocol(HttpProtocol.HTTPS);
        configuration.setHost("www.reedelk.com");
        configuration.setBasePath("/v3");

        // When
        String actualBaseUrl = BaseUrl.from(configuration);

        // Then
        assertThat(actualBaseUrl).isEqualTo("https://www.reedelk.com/v3");
    }

    @Test
    void shouldIgnoreProvidedSchemeAndReplaceItWithTheChosenHttpProtocol() {
        // Given
        RestClientConfiguration configuration = new RestClientConfiguration();
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setHost("https://www.reedelk.com");
        configuration.setBasePath("/v2");

        // When
        String actualBaseUrl = BaseUrl.from(configuration);

        // Then
        assertThat(actualBaseUrl).isEqualTo("http://www.reedelk.com/v2");
    }

    @Test
    void shouldThrowExceptionWhenHostIsEmpty() {
        // Given
        RestClientConfiguration configuration = new RestClientConfiguration();
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setHost("");
        configuration.setBasePath("/v1");

        // Expect
        ESBException thrown = assertThrows(ESBException.class, () -> BaseUrl.from(configuration));

        // Then
        assertThat(thrown.getMessage()).isEqualTo("'Host' must not be empty");
    }
}