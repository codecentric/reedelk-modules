package com.esb.foonnel.rest.commons;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UriTemplateTest {

    @Test
    public void shouldMatchGivenCallUri() {
        // Given
        String template = "/users/{groupId}/{securityLevel}";
        String callUri = "/users/group1/23";

        UriTemplate uriTemplate = new UriTemplate(template);

        // When
        boolean matches = uriTemplate.matches(callUri);

        // Then
        assertThat(matches).isTrue();
    }

    @Test
    public void shouldNotMatchGivenCallUri() {
        // Given
        String template = "/users/{groupId}";
        String callUri = "/users";

        UriTemplate uriTemplate = new UriTemplate(template);

        // When
        boolean matches = uriTemplate.matches(callUri);

        // Then
        assertThat(matches).isFalse();
    }

    @Test
    public void shouldMatchGivenCallUriWhenEmptyAfterSlash() {
        // Given
        String template = "/users/{groupId}";
        String callUri = "/users/";

        UriTemplate uriTemplate = new UriTemplate(template);

        // When
        boolean matches = uriTemplate.matches(callUri);

        // Then
        assertThat(matches).isTrue();
    }
}
