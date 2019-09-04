package com.reedelk.rest.component;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.configuration.RestMethod.POST;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;

class RestClientPostTest extends RestClientAbstractTest {

    @Test
    void shouldPostWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";

        mockServer.stubFor(post(urlEqualTo(path))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
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

    @Test
    void shouldPostWithEmptyBodyExecuteCorrectlyWhenResponse200() {
        // Given
        mockServer.stubFor(post(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody("It works")));

        RestClient component = componentWith(baseURL, path, POST);

        Message emptyPayload = MessageBuilder.get().build();

        // When
        Message outMessage = component.apply(emptyPayload);

        // Then
        assertThatContentIs(outMessage, "It works");
        assertThatMimeTypeIs(outMessage, TEXT);
    }

    @Test
    void shouldPostThrowExceptionWhenResponseNot2xx() {
        // Given
        mockServer.stubFor(post(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(500)
                        .withBody("Error exception caused by XYZ")));

        RestClient component = componentWith(baseURL, path, POST);

        Message emptyPayload = MessageBuilder.get().build();

        // When
        ESBException thrown = Assertions.assertThrows(ESBException.class,
                () -> component.apply(emptyPayload));

        assertThat(thrown).hasMessage("500 Internal Server Error");
    }
}
