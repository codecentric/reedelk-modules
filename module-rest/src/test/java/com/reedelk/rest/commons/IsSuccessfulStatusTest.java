package com.reedelk.rest.commons;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsSuccessfulStatusTest {

    @Test
    void shouldReturnTrue() {
        // Given
        int status = 280;

        // When
        boolean actual = IsSuccessfulStatus.status(status);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnFalse() {
        // Given
        int status = 579;

        // When
        boolean actual = IsSuccessfulStatus.status(status);

        // Then
        assertThat(actual).isFalse();
    }
}