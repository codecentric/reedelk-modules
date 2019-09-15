package com.reedelk.rest.commons;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsRedirectionTest {

    @Test
    void shouldReturnTrue() {
        // Given
        HttpResponseStatus status = HttpResponseStatus.valueOf(343);

        // When
        boolean actual = IsRedirection.status(status);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnFalse() {
        // Given
        HttpResponseStatus status = HttpResponseStatus.valueOf(200);

        // When
        boolean actual = IsRedirection.status(status);

        // Then
        assertThat(actual).isFalse();
    }
}