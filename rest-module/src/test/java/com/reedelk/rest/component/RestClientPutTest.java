package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.PUT;
import static com.reedelk.runtime.api.message.content.MimeType.TEXT;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

class RestClientPutTest extends RestClientAbstractTest {

    @Test
    void shouldWithBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String requestBody = "{\"Name\":\"John\"}";
        String expectedResponseBody = "PUT was successful";
        RestClient client = clientWith(PUT, BASE_URL, PATH, EVALUATE_PAYLOAD_BODY);

        doReturn(Optional.of(requestBody.getBytes()))
                .when(scriptEngine)
                .evaluate(eq(EVALUATE_PAYLOAD_BODY), any(Message.class), any(FlowContext.class));

        givenThat(put(urlEqualTo(PATH))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message payload = MessageBuilder.get().json(requestBody).build();

        // Expect
        AssertHttpResponse.isSuccessful(client, payload, flowContext, expectedResponseBody, TEXT);
    }

    @Test
    void shouldWithEmptyBodyExecuteCorrectlyWhenResponse200() {
        // Given
        String expectedResponseBody = "It works";
        RestClient client = clientWith(PUT, BASE_URL, PATH);

        doReturn(Optional.of(new byte[]{}))
                .when(scriptEngine)
                .evaluate(Mockito.isNull(DynamicByteArray.class), any(Message.class), any(FlowContext.class));

        givenThat(put(urlEqualTo(PATH))
                .withRequestBody(binaryEqualTo(new byte[0]))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withStatus(200)
                        .withBody(expectedResponseBody)));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(client, emptyPayload, flowContext, expectedResponseBody, TEXT);
    }

    @Test
    void shouldThrowExceptionWhenResponseNot2xx() {
        // Given
        String expectedErrorMessage = "Error exception caused by XYZ";
        RestClient component = clientWith(PUT, BASE_URL, PATH);

        givenThat(put(urlEqualTo(PATH))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody(expectedErrorMessage)));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isNotSuccessful(component, emptyPayload, flowContext, expectedErrorMessage);
    }
}
