package com.reedelk.rest.commons;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultsTest {

    @Test
    void shouldReturnDefaultPortWhenProtocolIsHTTP() {
        // Given
        Integer given = null;

        // When
        int actual = Defaults.RestListener.port(given, HttpProtocol.HTTP);

        // Then
        assertThat(actual).isEqualTo(8080);
    }

    @Test
    void shouldReturnDefaultPortWhenProtocolIsHTTPS() {
        // Given
        Integer given = null;

        // When
        int actual = Defaults.RestListener.port(given, HttpProtocol.HTTPS);

        // Then
        assertThat(actual).isEqualTo(8443);
    }

    @Test
    void shouldReturnGivenPortWhenProtocolIsHTTP() {
        // Given
        Integer given = 9975;

        // When
        int actual = Defaults.RestListener.port(given, HttpProtocol.HTTP);

        // Then
        assertThat(actual).isEqualTo(9975);
    }

    @Test
    void shouldReturnGivenPortWhenProtocolIsHTTPS() {
        // Given
        Integer given = 443;

        // When
        int actual = Defaults.RestListener.port(given, HttpProtocol.HTTPS);

        // Then
        assertThat(actual).isEqualTo(443);
    }
}