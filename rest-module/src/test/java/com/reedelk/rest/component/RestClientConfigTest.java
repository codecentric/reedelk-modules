package com.reedelk.rest.component;

import com.reedelk.rest.commons.ConfigurationException;
import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.Authentication;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestClientConfigTest extends RestClientAbstractTest {

    @Test
    void shouldThrowExceptionWhenDigestAuthenticationButNoConfigIsDefined() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setBasePath(PATH);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setAuthentication(Authentication.DIGEST);

        RestClient component = clientWith(RestMethod.GET, BASE_URL, PATH);
        component.setConfiguration(configuration);

        // Expect
        ConfigurationException thrown = assertThrows(ConfigurationException.class, () -> invoke(component));
        assertThat(thrown).hasMessage("Digest Authentication Configuration must be present in the JSON definition when auth type is 'DIGEST'");
    }

    @Test
    void shouldThrowExceptionWhenBasicAuthenticationButNoConfigIsDefined() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setBasePath(PATH);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setAuthentication(Authentication.BASIC);

        RestClient component = clientWith(RestMethod.GET, BASE_URL, PATH);
        component.setConfiguration(configuration);

        // Expect
        ConfigurationException thrown = assertThrows(ConfigurationException.class, () -> invoke(component));
        assertThat(thrown).hasMessage("Basic Authentication Configuration must be present in the JSON definition when auth type is 'BASIC'");
    }

    private void invoke(RestClient component) {
        Message payload = MessageBuilder.get().build();
        component.apply(payload, flowContext, new OnResult() {});
    }
}
