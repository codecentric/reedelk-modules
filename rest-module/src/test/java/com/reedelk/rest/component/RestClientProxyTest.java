package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.*;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class RestClientProxyTest extends RestClientAbstractTest {

    // We assume that the WireMock server is our proxy server
    private static final String PROXY_HOST = HOST;
    private static final int PROXY_PORT = PORT;

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyUseProxy() {
        // Given
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        proxyConfiguration.setHost(PROXY_HOST);
        proxyConfiguration.setPort(PROXY_PORT);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost("my-test-host.com");
        configuration.setPort(7891);
        configuration.setBasePath(path);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setProxy(Proxy.PROXY);
        configuration.setProxyConfiguration(proxyConfiguration);

        RestClient component = clientWith(RestMethod.GET, baseURL, path);
        component.setConfiguration(configuration);

        givenThat(any(urlEqualTo(path))
                .willReturn(aResponse().withStatus(200)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyUseProxyWithAuthentication() {
        // Given
        String username = "squid-user";
        String password = "squid-pass";
        ProxyAuthenticationConfiguration proxyAuthConfiguration = new ProxyAuthenticationConfiguration();
        proxyAuthConfiguration.setUsername(username);
        proxyAuthConfiguration.setPassword(password);

        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        proxyConfiguration.setAuthentication(ProxyAuthentication.USER_AND_PASSWORD);
        proxyConfiguration.setHost(PROXY_HOST);
        proxyConfiguration.setPort(PROXY_PORT);
        proxyConfiguration.setAuthenticationConfiguration(proxyAuthConfiguration);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost("my-test-host.com");
        configuration.setPort(7891);
        configuration.setBasePath(path);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setProxy(Proxy.PROXY);
        configuration.setProxyConfiguration(proxyConfiguration);

        RestClient component = clientWith(RestMethod.GET, baseURL, path);
        component.setConfiguration(configuration);

        givenThat(any(urlEqualTo(path))
                .withHeader("Authorization", StringValuePattern.ABSENT)
                .willReturn(aResponse()
                        .withHeader("WWW-Authenticate", "Basic realm=\"test-realm\"")
                        .withStatus(401)));

        givenThat(any(urlEqualTo(path))
                .withBasicAuth(username, password)
                .willReturn(aResponse().withStatus(200)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyUseProxyWithPreemptiveAuthentication() {
        // Given
        String username = "squid-user";
        String password = "squid-pass";
        ProxyAuthenticationConfiguration proxyAuthConfiguration = new ProxyAuthenticationConfiguration();
        proxyAuthConfiguration.setUsername(username);
        proxyAuthConfiguration.setPassword(password);
        proxyAuthConfiguration.setPreemptive(true);

        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        proxyConfiguration.setAuthentication(ProxyAuthentication.USER_AND_PASSWORD);
        proxyConfiguration.setHost(PROXY_HOST);
        proxyConfiguration.setPort(PROXY_PORT);
        proxyConfiguration.setAuthenticationConfiguration(proxyAuthConfiguration);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost("my-test-host.com");
        configuration.setPort(7891);
        configuration.setBasePath(path);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setProxy(Proxy.PROXY);
        configuration.setProxyConfiguration(proxyConfiguration);

        RestClient component = clientWith(RestMethod.GET, baseURL, path);
        component.setConfiguration(configuration);

        givenThat(any(urlEqualTo(path))
                .withBasicAuth(username, password)
                .willReturn(aResponse().withStatus(200)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext);
    }
}
