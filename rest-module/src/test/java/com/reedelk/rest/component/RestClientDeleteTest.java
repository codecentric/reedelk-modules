package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.configuration.RestMethod.DELETE;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;


class RestClientDeleteTest extends RestClientAbstractTest {

    @Test
    void shouldDeleteWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";

        mockServer.stubFor(
                delete(urlEqualTo(path))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader("Content-Type", TEXT.toString())
                        .withStatus(200)
                        .withBody("DELETE was successful")));

        RestClient component = componentWith(baseURL, path, DELETE);
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type", MimeType.APPLICATION_JSON.toString());
        component.setHeaders(headers);

        Message payload = MessageBuilder.get().json(requestBody).build();

        // When
        Message outMessage = component.apply(payload);

        // Then
        assertThatContentIs(outMessage, "DELETE was successful");
        assertThatMimeTypeIs(outMessage, TEXT);
    }

    @Test
    void shouldDeleteWithoutBodyExecuteCorrectlyWhenResponse200() {
        // Given
        mockServer.stubFor(delete(urlEqualTo(path))
                .willReturn(aResponse()
                        .withHeader("Content-Type", TEXT.toString())
                        .withStatus(200)
                        .withBody("It works")));

        RestClient component = componentWith(baseURL, path, DELETE);

        Message emptyPayload = MessageBuilder.get().build();

        // When
        Message outMessage = component.apply(emptyPayload);

        // Then
        assertThatContentIs(outMessage, "It works");
        assertThatMimeTypeIs(outMessage, TEXT);
    }
}
