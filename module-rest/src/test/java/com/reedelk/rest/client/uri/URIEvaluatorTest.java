package com.reedelk.rest.client.uri;

import com.reedelk.rest.ConfigurationException;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class URIEvaluatorTest {

    @Mock
    private ScriptEngineService mockScriptEngine;
    @Mock
    private Message message;
    @Mock
    private FlowContext context;

    @Test
    void shouldCorrectlyBuildUriWhenPortIsMissing() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost("localhost");

        URIEvaluator evaluator = URIEvaluator.builder()
                .configuration(configuration)
                .scriptEngine(mockScriptEngine)
                .build();

        URIProvider provider = evaluator.provider(message, context);

        // When
        URI uri = provider.uri();

        assertThat(uri.toString()).isEqualTo("http://localhost");
    }

    @Test
    void shouldThrowExceptionWhenHostIsMissing() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();

        // Expect
        ConfigurationException thrown = assertThrows(ConfigurationException.class,
                () -> URIEvaluator.builder()
                .configuration(configuration)
                .scriptEngine(mockScriptEngine)
                .build());

        assertThat(thrown).isNotNull();
    }

    @Test
    void shouldCorrectlyBuildUriWithBasePath() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost("localhost");
        configuration.setBasePath("/api");

        URIEvaluator evaluator = URIEvaluator.builder()
                .configuration(configuration)
                .scriptEngine(mockScriptEngine)
                .build();

        URIProvider provider = evaluator.provider(message, context);

        // When
        URI uri = provider.uri();

        assertThat(uri.toString()).isEqualTo("http://localhost/api");
    }

    @Test
    void shouldCorrectlyBuildUriWithCustomPortAndBasePath() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost("localhost");
        configuration.setPort(8867);
        configuration.setBasePath("/api");

        URIEvaluator evaluator = URIEvaluator.builder()
                .configuration(configuration)
                .scriptEngine(mockScriptEngine)
                .build();

        URIProvider provider = evaluator.provider(message, context);

        // When
        URI uri = provider.uri();

        // Then
        assertThat(uri.toString()).isEqualTo("http://localhost:8867/api");
    }

    @Test
    void shouldThrowExceptionWhenConfigAndBaseURLAreNull() {
        // Given
        ConfigurationException thrown = assertThrows(ConfigurationException.class,
                () -> URIEvaluator.builder()
                        .scriptEngine(mockScriptEngine)
                        .build());

        // Expect
        assertThat(thrown).isNotNull();
    }

    @Test
    void shouldReturnCorrectUrlWhenBaseURLIsPresent() {
        // Given
        URIEvaluator evaluator = URIEvaluator.builder()
                .baseURL("http://localhost/api/resource")
                .scriptEngine(mockScriptEngine)
                .build();

        URIProvider provider = evaluator.provider(message, context);

        // When
        URI uri = provider.uri();

        // Then
        assertThat(uri.toString()).isEqualTo("http://localhost/api/resource");
    }
}