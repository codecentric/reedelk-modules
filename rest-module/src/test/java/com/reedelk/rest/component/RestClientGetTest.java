package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.configuration.RestMethod.GET;
import static com.reedelk.runtime.api.message.type.MimeType.APPLICATION_JSON;

class RestClientGetTest extends RestClientAbstractTest {

    @Test
    void shouldGetExecuteCorrectlyWhenResponse200() throws InterruptedException {
        // Given
        String responseBody = "{\"Name\":\"John\"}";

        mockServer.stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON.toString())
                        .withStatus(200)
                        .withBody(responseBody)));

        RestClient component = componentWith(baseURL, path, GET);

        Message payload = MessageBuilder.get().build();

        // When
        Message outMessage = component.apply(payload);

        // Then
        assertThatContentIs(outMessage, responseBody);
        assertThatMimeTypeIs(outMessage, APPLICATION_JSON);
    }
}
