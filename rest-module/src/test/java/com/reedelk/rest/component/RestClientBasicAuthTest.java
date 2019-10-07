package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.Authentication;
import com.reedelk.rest.configuration.client.BasicAuthenticationConfiguration;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class RestClientBasicAuthTest extends RestClientAbstractTest {

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyPerformBasicAuthentication(String method) {
        // Given
        String username = "test123";
        String password = "pass123";
        BasicAuthenticationConfiguration basicAuth = new BasicAuthenticationConfiguration();
        basicAuth.setPassword(password);
        basicAuth.setUsername(username);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setBasePath(path);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setAuthentication(Authentication.BASIC);
        configuration.setBasicAuthentication(basicAuth);

        RestClient component = clientWith(RestMethod.valueOf(method), baseURL, path);
        component.setConfiguration(configuration);

        givenThat(any(urlEqualTo(path))
                .withHeader("Authorization", StringValuePattern.ABSENT)
                .willReturn(aResponse()
                        .withHeader("WWW-Authenticate", "Basic dGVzdDEyMzpwYXNzMTIz")
                        .withStatus(401)));

        givenThat(any(urlEqualTo(path))
                .withHeader("Authorization", matching("Basic .*"))
                .willReturn(aResponse()
                        .withStatus(200)));

        givenThat(any(urlEqualTo(path))
                .withBasicAuth(username, password)
                .willReturn(aResponse().withStatus(200)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext);
    }


    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyPerformBasicAuthenticationWithPreemptive(String method) {
        // Given
        String username = "test123";
        String password = "pass123";
        BasicAuthenticationConfiguration basicAuth = new BasicAuthenticationConfiguration();
        basicAuth.setPassword(password);
        basicAuth.setUsername(username);
        basicAuth.setPreemptive(true);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setBasePath(path);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setAuthentication(Authentication.BASIC);
        configuration.setBasicAuthentication(basicAuth);

        RestClient component = clientWith(RestMethod.valueOf(method), baseURL, path);
        component.setConfiguration(configuration);

        givenThat(any(urlEqualTo(path))
                .withBasicAuth(username, password)
                .willReturn(aResponse().withStatus(200)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext);
    }
}
