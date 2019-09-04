package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.configuration.RestMethod.POST;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;

class RestClientPostTest extends RestClientAbstractTest {

    @Test
    void shouldPostWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";

        mockServer.stubFor(post(urlEqualTo(path))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader("Content-Type", TEXT.toString())
                        .withStatus(200)
                        .withBody("POST was successful")));

        RestClient component = componentWith(baseURL, path, POST);

        Message payload = MessageBuilder.get().json(requestBody).build();

        // When
        Message outMessage = component.apply(payload);

        // Then
        assertThatContentIs(outMessage, "POST was successful");
        assertThatMimeTypeIs(outMessage, TEXT);
    }
}
