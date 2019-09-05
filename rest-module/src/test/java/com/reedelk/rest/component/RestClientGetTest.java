package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.configuration.RestMethod.GET;
import static com.reedelk.runtime.api.message.type.MimeType.APPLICATION_JSON;

class RestClientGetTest extends RestClientAbstractTest {

    private RestClient component = componentWith(baseURL, path, GET);

    @Test
    void shouldGetExecuteCorrectlyWhenResponse200() {
        // Given
        String responseBody = "{\"Name\":\"John\"}";

        mockServer.stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                        .withStatus(200)
                        .withBody(responseBody)));

        Message payload = MessageBuilder.get().build();

        // When
        Message outMessage = component.apply(payload);

        // Then
        assertContentIs(outMessage, responseBody, APPLICATION_JSON);
    }
}
