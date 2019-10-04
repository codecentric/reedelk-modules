package com.reedelk.rest.component;

import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.configuration.client.Authentication;
import com.reedelk.rest.configuration.client.BasicAuthenticationConfiguration;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.GET;
import static com.reedelk.runtime.api.message.type.MimeType.APPLICATION_JSON;

class RestClientBasicAuthTest extends RestClientAbstractTest {

    @Test
    void shouldGetExecuteCorrectlyWithAuth() {
        // Given
        BasicAuthenticationConfiguration basicAuthenticationConfiguration = new BasicAuthenticationConfiguration();
        basicAuthenticationConfiguration.setPassword("mypass");
        basicAuthenticationConfiguration.setUsername("myuser");
        basicAuthenticationConfiguration.setPreemptive(true);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setBasePath(path);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setAuthentication(Authentication.BASIC);
        configuration.setBasicAuthentication(basicAuthenticationConfiguration);

        String responseBody = "{\"Name\":\"John\"}";
        RestClient component = clientWith(GET, baseURL, path);
        component.setConfiguration(configuration);

        givenThat(get(urlEqualTo(path)).withBasicAuth("myuser", "mypass")
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                        .withStatus(200)
                        .withBody(responseBody)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse
                .isSuccessful(component, payload, flowContext, responseBody, APPLICATION_JSON);
    }
}
