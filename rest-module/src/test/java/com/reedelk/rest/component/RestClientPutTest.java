package com.reedelk.rest.component;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.configuration.RestMethod.PUT;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestClientPutTest extends RestClientAbstractTest {

    private RestClient component = componentWith(baseURL, path, PUT);

    @Test
    void shouldPutWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";
        String expectedResponseBody = "PUT was successful";

        mockServer.stubFor(put(urlEqualTo(path))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message payload = MessageBuilder.get().json(requestBody).build();

        // When
        Message outMessage = component.apply(payload, context);

        // Then
        assertContentIs(outMessage, expectedResponseBody, TEXT);
    }

    @Test
    void shouldPutWithEmptyBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String expectedResponseBody = "It works";

        mockServer.stubFor(put(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message emptyPayload = MessageBuilder.get().build();

        // When
        Message outMessage = component.apply(emptyPayload, context);

        // Then
        assertContentIs(outMessage, expectedResponseBody, TEXT);
    }

    @Test
    void shouldPostThrowExceptionWhenResponseNot2xx() {
        // Given
        mockServer.stubFor(put(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody("Error exception caused by XYZ")));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        ESBException thrown = assertThrows(ESBException.class,
                () -> component.apply(emptyPayload, context));

        assertThat(thrown).hasMessage("404 Not Found");
    }
}
