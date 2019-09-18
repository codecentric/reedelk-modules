package com.reedelk.rest.component;

import static com.reedelk.rest.commons.RestMethod.PUT;

class RestClientPutTest extends RestClientAbstractTest {

    private RestClient component = componentWith(baseURL, path, PUT);

    /**
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
        component.setBody(ScriptUtils.EVALUATE_PAYLOAD);
        Message outMessage = component.apply(payload, flowContext);

        // Then
        assertContent(outMessage, expectedResponseBody, TEXT);
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
        Message outMessage = component.apply(emptyPayload, flowContext);

        // Then
        assertContent(outMessage, expectedResponseBody, TEXT);
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
                () -> component.apply(emptyPayload, flowContext));

        assertThat(thrown).hasMessage("Error exception caused by XYZ");
    }*/
}
