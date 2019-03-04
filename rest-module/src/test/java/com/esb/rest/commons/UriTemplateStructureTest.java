package com.esb.rest.commons;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class UriTemplateStructureTest {

    @Test
    public void shouldComputeTemplateRegexPatternCorrectly() {
        // Given
        String template = "/api/users/{code}/{group}";

        // When
        UriTemplateStructure structure = UriTemplateStructure.from(template);

        // Then
        Pattern pattern = structure.getPattern();
        String computedPattern = pattern.toString();
        assertThat(computedPattern).isEqualTo("\\Q/api/users/\\E([^/]*)\\Q/\\E([^/]*)");
    }

    @Test
    public void shouldComputeTemplateVariableNamesCorrectly() {
        // Given
        String template = "/api/test/{var1}/{var2}/{var3}";

        // When
        UriTemplateStructure structure = UriTemplateStructure.from(template);

        // Then
        Collection<String> variableNames = structure.getVariableNames();
        assertThat(variableNames).containsExactlyInAnyOrder("var1", "var2", "var3");
    }

    @Test
    public void shouldReturnEmptyListWhenNoVariablesDefinedInTemplate() {
        // Given
        String template = "/api/test/customer";

        // When
        UriTemplateStructure structure = UriTemplateStructure.from(template);

        // Then
        assertThat(structure.getVariableNames()).isEmpty();
    }

}
