package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.DELETE;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

class RestClientDeleteTest extends RestClientAbstractTest {

    @Test
    void shouldDeleteWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";
        String expectedResponseBody = "DELETE was successful";
        RestClient component = componentWith(DELETE, baseURL, path, EVALUATE_PAYLOAD_BODY);

        doReturn(Optional.of(requestBody.getBytes()))
                .when(scriptEngine)
                .evaluate(eq(EVALUATE_PAYLOAD_BODY), any(Message.class), any(FlowContext.class));

        givenThat(delete(urlEqualTo(path))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message payload = MessageBuilder.get().json(requestBody).build();

        // Expect
        AssertHttpResponse
                .isSuccessful(component, payload, flowContext, expectedResponseBody, TEXT);
    }

    @Test
    void shouldDeleteWithEmptyBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String expectedResponseBody = "It works";
        RestClient component = componentWith(DELETE, baseURL, path);

        givenThat(delete(urlEqualTo(path))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse
                .isSuccessful(component, emptyPayload, flowContext, expectedResponseBody, TEXT);
    }

    @Test
    void shouldDeleteThrowExceptionWhenResponseNot2xx() {
        // Given
        String expectedErrorMessage = "Error exception caused by XYZ";
        RestClient component = componentWith(DELETE, baseURL, path);

        givenThat(delete(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(507)
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody(expectedErrorMessage)));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse
                .isNotSuccessful(component, emptyPayload, flowContext, expectedErrorMessage);
    }
}
