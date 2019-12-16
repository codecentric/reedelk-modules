package com.reedelk.rest.server.uri;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UriTemplateTest {

    @Test
    void shouldMatchGivenCallUri() {
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
    void shouldNotMatchGivenCallUri() {
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
    void shouldMatchGivenCallUriWhenEmptyAfterSlash() {
        // Given
        String template = "/users/{groupId}";
        String callUri = "/users/";

        UriTemplate uriTemplate = new UriTemplate(template);

        // When
        boolean matches = uriTemplate.matches(callUri);

        // Then
        assertThat(matches).isTrue();
    }

    @Test
    void shouldBindVariableValuesCorrectly() {
        // Given
        String template = "/users/{groupId}/{securityLevel}";
        String callUri = "/users/admins/34";

        UriTemplate uriTemplate = new UriTemplate(template);

        // When
        Map<String, String> bindings = uriTemplate.bind(callUri);

        // Then
        assertThat(bindings).hasSize(2);
        assertThat(bindings).containsKeys("groupId", "securityLevel");
        assertThat(bindings.get("groupId")).isEqualTo("admins");
        assertThat(bindings.get("securityLevel")).isEqualTo("34");
    }

    @Test
    void shouldBindVariableValuesCorrectlyWhenQueryParametersArePresent() {
        // Given
        String template = "/users/{groupId}/{securityLevel}";
        String callUri = "/users/admins/34?queryParam1=queryParam1Value&queryParam2=queryParam2Value";

        UriTemplate uriTemplate = new UriTemplate(template);

        // When
        Map<String, String> bindings = uriTemplate.bind(callUri);

        // Then
        assertThat(bindings).hasSize(2);
        assertThat(bindings).containsKeys("groupId", "securityLevel");
        assertThat(bindings.get("groupId")).isEqualTo("admins");
        assertThat(bindings.get("securityLevel")).isEqualTo("34");
    }

    @Test
    void shouldBindVariableWithCustomRegexp() {
        // Given
        String template = "/web/{page:.*}";
        String callUri = "/web/assets/javascript/index.js";

        UriTemplate uriTemplate = new UriTemplate(template);

        // When
        Map<String, String> bindings = uriTemplate.bind(callUri);

        // Then
        assertThat(bindings).containsEntry("page", "assets/javascript/index.js");
    }
}
