package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.reedelk.rest.ConfigurationException;
import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.*;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.RestMethod.GET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestClientProxyTest extends RestClientAbstractTest {

    // We assume that the WireMock server is our proxy server
    private static final String PROXY_HOST = HOST;
    private static final int PROXY_PORT = PORT;

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyUseProxy(String method) {
        // Given
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        proxyConfiguration.setHost(PROXY_HOST);
        proxyConfiguration.setPort(PROXY_PORT);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost("my-test-host.com");
        configuration.setPort(7891);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setProxy(Proxy.PROXY);
        configuration.setProxyConfiguration(proxyConfiguration);

        RestClient component = clientWith(RestMethod.valueOf(method), configuration, PATH);

        givenThat(any(urlEqualTo(PATH))
                .withHeader("Host", equalTo("my-test-host.com:7891"))
                .willReturn(aResponse().withStatus(200)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext);
    }

    @Test
    void shouldThrowExceptionWhenProxyButNoConfigIsDefined() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setProxy(Proxy.PROXY);

        RestClient component = clientWith(GET, configuration, PATH);

        // Expect
        ConfigurationException thrown = assertThrows(ConfigurationException.class, () -> invoke(component));
        assertThat(thrown).hasMessage("Proxy Configuration must be present in the JSON definition when 'proxy' property is 'PROXY'");
    }

    @DisplayName("Proxy Digest Authentication tests")
    @Nested
    class ProxyDigestAuthenticationTests {

        @ParameterizedTest
        @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
        void shouldCorrectlyUseProxyWithDigestAuthentication(String method) {
            // Given
            String username = "squid-user";
            String password = "squid-pass";
            ProxyDigestAuthenticationConfiguration proxyAuthConfiguration = new ProxyDigestAuthenticationConfiguration();
            proxyAuthConfiguration.setUsername(username);
            proxyAuthConfiguration.setPassword(password);

            ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
            proxyConfiguration.setHost(PROXY_HOST);
            proxyConfiguration.setPort(PROXY_PORT);
            proxyConfiguration.setAuthentication(ProxyAuthentication.DIGEST);
            proxyConfiguration.setDigestAuthentication(proxyAuthConfiguration);

            ClientConfiguration configuration = new ClientConfiguration();
            configuration.setHost("my-test-host.com");
            configuration.setPort(7891);
            configuration.setProtocol(HttpProtocol.HTTP);
            configuration.setId(UUID.randomUUID().toString());
            configuration.setProxy(Proxy.PROXY);
            configuration.setProxyConfiguration(proxyConfiguration);

            RestClient component = clientWith(RestMethod.valueOf(method), configuration, PATH);

            givenThat(any(urlEqualTo(PATH))
                    .withHeader("Proxy-Authorization", StringValuePattern.ABSENT)
                    .willReturn(aResponse()
                            .withHeader("Proxy-Authenticate", "Digest realm=\"SurfinUSA\", nonce=\"zIqdXQAAAAAA7cOwoH8AAOo8zEoAAAAA\", qop=\"auth\", stale=false")
                            .withStatus(407)));

            givenThat(any(urlEqualTo(PATH))
                    .withHeader("Proxy-Authorization", matching("Digest username=\"squid-user\", realm=\"SurfinUSA\",.*"))
                    .willReturn(aResponse().withStatus(200)));

            Message payload = MessageBuilder.get().build();

            // Expect
            AssertHttpResponse.isSuccessful(component, payload, flowContext);
        }
    }

    @DisplayName("Proxy Basic Authentication tests")
    @Nested
    class ProxyBasicAuthenticationTests {

        @ParameterizedTest
        @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
        void shouldCorrectlyUseProxyWithBasicAuthentication(String method) {
            // Given
            String username = "squid-user";
            String password = "squid-pass";
            ProxyBasicAuthenticationConfiguration proxyAuthConfiguration = new ProxyBasicAuthenticationConfiguration();
            proxyAuthConfiguration.setUsername(username);
            proxyAuthConfiguration.setPassword(password);

            ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
            proxyConfiguration.setHost(PROXY_HOST);
            proxyConfiguration.setPort(PROXY_PORT);
            proxyConfiguration.setAuthentication(ProxyAuthentication.BASIC);
            proxyConfiguration.setBasicAuthentication(proxyAuthConfiguration);

            ClientConfiguration configuration = new ClientConfiguration();
            configuration.setHost("my-test-host.com");
            configuration.setPort(7891);
            configuration.setProtocol(HttpProtocol.HTTP);
            configuration.setId(UUID.randomUUID().toString());
            configuration.setProxy(Proxy.PROXY);
            configuration.setProxyConfiguration(proxyConfiguration);

            RestClient component = clientWith(RestMethod.valueOf(method), configuration, PATH);

            givenThat(any(urlEqualTo(PATH))
                    .withHeader("Proxy-Authorization", StringValuePattern.ABSENT)
                    .willReturn(aResponse()
                            .withHeader("Proxy-Authenticate", "Basic realm=\"Authentication realm\"")
                            .withStatus(407)));

            givenThat(any(urlEqualTo(PATH))
                    .withHeader("Proxy-Authorization", equalTo("Basic c3F1aWQtdXNlcjpzcXVpZC1wYXNz"))
                    .willReturn(aResponse().withStatus(200)));

            Message payload = MessageBuilder.get().build();

            // Expect
            AssertHttpResponse.isSuccessful(component, payload, flowContext);
        }
    }
}
