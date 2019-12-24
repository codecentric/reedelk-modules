package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.Authentication;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.rest.configuration.client.DigestAuthenticationConfiguration;
import com.reedelk.runtime.api.exception.ConfigurationException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.RestMethod.GET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestClientDigestAuthTest extends RestClientAbstractTest {

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyPerformDigestAuthentication(String method) {
        // Given
        String username = "test123";
        String password = "pass123";
        DigestAuthenticationConfiguration digestAuth = new DigestAuthenticationConfiguration();
        digestAuth.setPassword(password);
        digestAuth.setUsername(username);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setAuthentication(Authentication.DIGEST);
        configuration.setDigestAuthentication(digestAuth);

        RestClient component = clientWith(RestMethod.valueOf(method), configuration, PATH);


        givenThat(any(urlEqualTo(PATH))
                .withHeader("Authorization", StringValuePattern.ABSENT)
                .willReturn(aResponse()
                        .withHeader("WWW-Authenticate", "Digest realm=\"testrealm@host.com\"," +
                                "qop=\"auth,auth-int\"," +
                                "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," +
                                "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"")
                        .withStatus(401)));

        givenThat(any(urlEqualTo(PATH))
                .withHeader("Authorization", matching("Digest username=\"test123\", realm=\"testrealm@host.com\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", uri=\"/v1/resource\", response=.*"))
                .willReturn(aResponse()
                        .withStatus(200)));


        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyPerformDigestAuthenticationWithPreemptive(String method) {
        // Given
        String username = "test123";
        String password = "pass123";
        DigestAuthenticationConfiguration digestAuth = new DigestAuthenticationConfiguration();
        digestAuth.setPassword(password);
        digestAuth.setUsername(username);
        digestAuth.setPreemptive(true);
        digestAuth.setRealm("test.realm@host.com");
        digestAuth.setNonce("noncetest");

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setAuthentication(Authentication.DIGEST);
        configuration.setDigestAuthentication(digestAuth);

        RestClient component = clientWith(RestMethod.valueOf(method), configuration, PATH);

        givenThat(any(urlEqualTo(PATH))
                .withHeader("Authorization", matching("Digest username=\"test123\", realm=\"test.realm@host.com\", nonce=\"noncetest\", uri=\"/v1/resource\", response=.*"))
                .willReturn(aResponse().withStatus(200)));


        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext);
    }

    @Test
    void shouldThrowExceptionWhenDigestAuthenticationButNoConfigIsDefined() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setAuthentication(Authentication.DIGEST);

        RestClient restClient = new RestClient();
        restClient.setConfiguration(configuration);
        restClient.setMethod(GET);
        restClient.setPath(PATH);
        setScriptEngine(restClient);
        setClientFactory(restClient);

        // Expect
        ConfigurationException thrown = assertThrows(ConfigurationException.class, restClient::initialize);
        assertThat(thrown).hasMessage("Digest Authentication Configuration must be present in the JSON definition when 'authentication' property is 'DIGEST'");
    }
}
