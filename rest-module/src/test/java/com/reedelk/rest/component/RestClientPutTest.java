package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.PUT;
import static com.reedelk.runtime.api.commons.ScriptUtils.EVALUATE_PAYLOAD;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;

class RestClientPutTest extends RestClientAbstractTest {

    @Test
    void shouldPutWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";
        String expectedResponseBody = "PUT was successful";
        RestClient component = componentWith(PUT, baseURL, path, EVALUATE_PAYLOAD);

        givenThat(put(urlEqualTo(path))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message payload = MessageBuilder.get().json(requestBody).build();

        // Expect
        AssertThatHttpResponseContent
                .isSuccessful(component, payload, flowContext, expectedResponseBody, TEXT);
    }

    @Test
    void shouldPutWithEmptyBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String expectedResponseBody = "It works";
        RestClient component = componentWith(PUT, baseURL, path);

        givenThat(put(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        AssertThatHttpResponseContent
                .isSuccessful(component, emptyPayload, flowContext, expectedResponseBody, TEXT);
    }

    @Test
    void shouldPostThrowExceptionWhenResponseNot2xx() {
        // Given
        String expectedErrorMessage = "Error exception caused by XYZ";
        RestClient component = componentWith(PUT, baseURL, path);

        givenThat(put(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody(expectedErrorMessage)));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        AssertThatHttpResponseContent
                .isNotSuccessful(component, emptyPayload, flowContext, expectedErrorMessage);
    }
}
