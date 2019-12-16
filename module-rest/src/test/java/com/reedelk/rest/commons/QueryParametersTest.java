package com.reedelk.rest.commons;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class QueryParametersTest {

    @Test
    void shouldMapParametersCorrectly() {
        // Given
        String uri = "/resource/34/group/user?queryParam1=queryValue1a&queryParam1=queryValue1b&queryParam2=queryValue2";

        // When
        HashMap<String, List<String>> queryParameters = QueryParameters.from(uri);

        // Then
        assertThat(queryParameters).hasSize(2);
        assertThat(queryParameters).containsEntry("queryParam1", asList("queryValue1a", "queryValue1b"));
        assertThat(queryParameters).containsEntry("queryParam2", Collections.singletonList("queryValue2"));
    }

    @Test
    void shouldReturnEmptyMapWhenNoQueryParameters() {
        // Given
        String uri = "/resource/34/group/user";

        // When
        HashMap<String, List<String>> queryParameters = QueryParameters.from(uri);

        // Then
        assertThat(queryParameters).isEmpty();
    }

    @Test
    void shouldReturnEmptyMapWhenUriIsNull() {
        // Given
        String uri = null;

        // When
        HashMap<String, List<String>> queryParameters = QueryParameters.from(uri);

        // Then
        assertThat(queryParameters).isEmpty();
    }

    @Test
    void shouldReturnEmptyMapWhenUriIsEmpty() {
        // Given
        String uri = "";

        // When
        HashMap<String, List<String>> queryParameters = QueryParameters.from(uri);

        // Then
        assertThat(queryParameters).isEmpty();
    }
}