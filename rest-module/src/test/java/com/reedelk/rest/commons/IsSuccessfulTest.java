package com.reedelk.rest.commons;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsSuccessfulTest {

    @Test
    void shouldReturnTrue() {
        // Given
        HttpResponseStatus status = HttpResponseStatus.valueOf(280);

        // When
        boolean actual = IsSuccessful.status(status);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnFalse() {
        // Given
        HttpResponseStatus status = HttpResponseStatus.valueOf(579);

        // When
        boolean actual = IsSuccessful.status(status);

        // Then
        assertThat(actual).isFalse();
    }
}