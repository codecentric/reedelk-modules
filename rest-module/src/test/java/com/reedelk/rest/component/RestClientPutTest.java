package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.configuration.RestMethod.PUT;
import static com.reedelk.runtime.api.message.type.MimeType.APPLICATION_JSON;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;

class RestClientPutTest extends RestClientAbstractTest {

    @Test
    void shouldPutWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";

        mockServer.stubFor(put(urlEqualTo(path))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader("Content-Type", TEXT.toString())
                        .withStatus(200)
                        .withBody("PUT was successful")));

        RestClient component = componentWith(baseURL, path, PUT);
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type", APPLICATION_JSON.toString());
        component.setHeaders(headers);

        Message payload = MessageBuilder.get().json(requestBody).build();

        // When
        Message outMessage = component.apply(payload);

        // Then
        assertThatContentIs(outMessage, "PUT was successful");
        assertThatMimeTypeIs(outMessage, TEXT);
    }
}
