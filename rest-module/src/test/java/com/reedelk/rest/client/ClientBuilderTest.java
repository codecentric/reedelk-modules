package com.reedelk.rest.client;

import com.reedelk.rest.configuration.ClientConfiguration;
import com.reedelk.rest.configuration.HttpProtocol;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.exception.ESBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClientRequest;

import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ClientBuilderTest {

    private ClientBuilder builder;
    private HttpClientWrapper mockWrapper;

    @BeforeEach
    void setUp() {
        mockWrapper = mock(HttpClientWrapper.class);
        builder = spy(ClientBuilder.get());
        doReturn(mockWrapper)
                .when(builder)
                .newWrapper();
    }

    @Test
    void shouldBuildClientWrapperCorrectlyWhenUseConfigurationIsFalse() {
        // Given
        String expectedBaseUrl = "http://www.reedelk.com:8080"; //todo: verify taht it answer on the correct port..
        builder.useConfiguration(false)
                .onRequestConsumer(requestConsumer)
                .method(RestMethod.GET)
                .baseUrl(expectedBaseUrl);


        // When
        HttpClientWrapper actual = builder.build();

        // Then
        verify(mockWrapper).doOnRequest(requestConsumer);
        verify(mockWrapper).baseURL(expectedBaseUrl);
        verify(mockWrapper).method(RestMethod.GET);
        assertThat(actual).isEqualTo(mockWrapper);
    }

    // We test that the application appends automatically http at the front of the base url if it is missing.
    @Test
    void shouldBuildClientWrapperCorrectlyWhenUseConfigurationIsFalseAndBaseUrlDoesNotContainScheme() {
        // Given
        String baseUrl = "www.reedelk.com";
        builder.useConfiguration(false)
                .onRequestConsumer(requestConsumer)
                .method(RestMethod.HEAD)
                .baseUrl(baseUrl);

        // When
        HttpClientWrapper actual = builder.build();

        // Then
        String expectedBaseUrl = "http://www.reedelk.com";
        verify(mockWrapper).baseURL(expectedBaseUrl);
        assertThat(actual).isEqualTo(mockWrapper);
    }

    @Test
    void shouldBuildClientWrapperThrowExceptionWhenBaseUrlIsNotValidUrlAndUseConfigurationIsFalse() {
        // Given
        String notValidUrl = "httpsui://:8024/$@notValidUrl";
        builder.useConfiguration(false)
                .onRequestConsumer(requestConsumer)
                .method(RestMethod.OPTIONS)
                .baseUrl(notValidUrl);

        // Expect
        ESBException thrown =
                assertThrows(ESBException.class, () -> builder.build());

        // Then
        assertThat(thrown.getMessage()).isEqualTo("Base URL is not a valid URL");
    }

    @Test
    void shouldBuildClientWrapperThrowExceptionWhenBaseUrlIsNullAndUseConfigurationIsFalse() {
        // Given
        builder.useConfiguration(false)
                .onRequestConsumer(requestConsumer)
                .method(RestMethod.DELETE)
                .baseUrl(null);

        // When
        ESBException thrown =
                assertThrows(ESBException.class, () -> builder.build());

        // Then
        assertThat(thrown.getMessage()).isEqualTo("Base URL must not to be null");
    }

    @Test
    void shouldBuildClientWrapperThrowExceptionWhenMethodIsNullAndUseConfigurationIsFalse() {
        // Given
        builder.useConfiguration(false)
                .onRequestConsumer(requestConsumer)
                .baseUrl("www.reedelk.com");

        // When
        ESBException thrown =
                assertThrows(ESBException.class, () -> builder.build());

        // Then
        assertThat(thrown.getMessage()).isEqualTo("HTTP method must not be null");
    }

    @Test
    void shouldBuildClientWrapperCorrectlyWhenUseConfigurationIsTrue() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setBasePath("/v1");
        configuration.setHost("www.reedelk.com");
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setPort(7618);

        builder.useConfiguration(true)
                .onRequestConsumer(requestConsumer)
                .method(RestMethod.POST)
                .configuration(configuration);

        // When
        HttpClientWrapper actual = builder.build();

        // Then
        verify(mockWrapper, never()).followRedirects(anyBoolean());
        verify(mockWrapper, never()).keepAlive(anyBoolean());
        verify(mockWrapper).doOnRequest(requestConsumer);
        verify(mockWrapper).port(7618);
        verify(mockWrapper).baseURL("http://www.reedelk.com/v1");
        verify(mockWrapper).method(RestMethod.POST);
        assertThat(actual).isEqualTo(mockWrapper);
    }

    @Test
    void shouldBuildClientWrapperCorrectlyNotAssignPortWhenPortIsNullWhenUseConfigurationIsTrue() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setBasePath("/v1");
        configuration.setHost("www.reedelk.com");
        configuration.setProtocol(HttpProtocol.HTTP);

        builder.useConfiguration(true)
                .onRequestConsumer(requestConsumer)
                .method(RestMethod.POST)
                .configuration(configuration);

        // When
        HttpClientWrapper actual = builder.build();

        // Then
        verify(mockWrapper, never()).port(anyInt());
        assertThat(actual).isEqualTo(mockWrapper);
    }

    @Test
    void shouldBuildClientWrapperCorrectlyAndSetFollowRedirectsWhenUseConfigurationIsTrue() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setBasePath("/v1");
        configuration.setHost("www.reedelk.com");
        configuration.setFollowRedirects(false);
        configuration.setProtocol(HttpProtocol.HTTP);

        builder.useConfiguration(true)
                .onRequestConsumer(requestConsumer)
                .method(RestMethod.DELETE)
                .configuration(configuration);

        // When
        HttpClientWrapper actual = builder.build();

        // Then
        verify(mockWrapper).followRedirects(false);
        assertThat(actual).isEqualTo(mockWrapper);
    }

    @Test
    void shouldBuildClientWrapperCorrectlyAndSetKeepAliveWhenUseConfigurationIsTrue() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setBasePath("/v1");
        configuration.setHost("www.reedelk.com");
        configuration.setKeepAlive(true);
        configuration.setProtocol(HttpProtocol.HTTP);

        builder.useConfiguration(true)
                .onRequestConsumer(requestConsumer)
                .method(RestMethod.OPTIONS)
                .configuration(configuration);

        // When
        HttpClientWrapper actual = builder.build();

        // Then
        verify(mockWrapper).keepAlive(true);
        assertThat(actual).isEqualTo(mockWrapper);
    }

    @Test
    void shouldBuildClientWrapperThrowExceptionWhenConfigurationIsNullAndUseConfigurationIsTrue() {
        // Given
        builder.useConfiguration(true)
                .onRequestConsumer(requestConsumer)
                .method(RestMethod.HEAD);

        // When
        ESBException thrown =
                assertThrows(ESBException.class, () -> builder.build());

        assertThat(thrown.getMessage()).isEqualTo("Configuration must not be null");
    }

    @Test
    void shouldBuildClientWrapperThrowExceptionWhenMethodIsNullAndUseConfigurationIsTrue() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        builder.useConfiguration(true)
                .onRequestConsumer(requestConsumer)
                .configuration(configuration);

        // When
        ESBException thrown =
                assertThrows(ESBException.class, () -> builder.build());

        assertThat(thrown.getMessage()).isEqualTo("HTTP method must not be null");
    }

    private BiConsumer<HttpClientRequest,Connection> requestConsumer = (request, connection) -> {
        // nothing
    };
}