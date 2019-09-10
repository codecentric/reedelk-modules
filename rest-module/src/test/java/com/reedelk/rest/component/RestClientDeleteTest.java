package com.reedelk.rest.component;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.DELETE;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;


class RestClientDeleteTest extends RestClientAbstractTest {

    private RestClient component = componentWith(baseURL, path, DELETE);

    @Test
    void shouldDeleteWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";
        String expectedResponseBody = "DELETE was successful";

        mockServer.stubFor(delete(urlEqualTo(path))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message payload = MessageBuilder.get().json(requestBody).build();

        // When
        Message outMessage = component.apply(payload, flowContext);

        // Then
        assertContentIs(outMessage, expectedResponseBody, TEXT);
    }

    @Test
    void shouldDeleteWithEmptyBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String expectedResponseBody = "It works";

        mockServer.stubFor(delete(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message emptyPayload = MessageBuilder.get().build();

        // When
        Message outMessage = component.apply(emptyPayload, flowContext);

        // Then
        assertContentIs(outMessage, expectedResponseBody, TEXT);
    }

    @Test
    void shouldDeleteThrowExceptionWhenResponseNot2xx() {
        // Given
        mockServer.stubFor(delete(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withStatus(507)
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody("Error exception caused by XYZ")));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        ESBException thrown = Assertions.assertThrows(ESBException.class,
                () -> component.apply(emptyPayload, flowContext));

        assertThat(thrown).hasMessage("507 Insufficient Storage");
    }
}
