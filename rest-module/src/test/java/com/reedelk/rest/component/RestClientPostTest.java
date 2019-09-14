package com.reedelk.rest.component;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.POST;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestClientPostTest extends RestClientAbstractTest {

    private RestClient component = componentWith(baseURL, path, POST);

    @Test
    void shouldPostWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";
        String expectedResponseBody = "POST was successful";

        mockServer.stubFor(post(urlEqualTo(path))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message payload = MessageBuilder.get().json(requestBody).build();

        // When
        component.setBody(ScriptUtils.EVALUATE_PAYLOAD);
        Message outMessage = component.apply(payload, flowContext);

        // Then
        assertContentIs(outMessage, expectedResponseBody, TEXT);
    }

    @Test
    void shouldPostWithEmptyBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String expectedResponseBody = "It works";

        mockServer.stubFor(post(urlEqualTo(path))
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
    void shouldPostThrowExceptionWhenResponseNot2xx() {
        // Given
        mockServer.stubFor(post(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(500)
                        .withBody("Error exception caused by XYZ")));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        ESBException thrown = assertThrows(ESBException.class,
                () -> component.apply(emptyPayload, flowContext));

        assertThat(thrown).hasMessage("Error exception caused by XYZ");
    }
}
