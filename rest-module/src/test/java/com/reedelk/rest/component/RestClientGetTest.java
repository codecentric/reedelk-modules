package com.reedelk.rest.component;

import static com.reedelk.rest.commons.RestMethod.GET;

class RestClientGetTest extends RestClientAbstractTest {

    private RestClient component = componentWith(baseURL, path, GET);
/**
    @Test
    void shouldGetExecuteCorrectlyWhenResponse200() {
        // Given
        String responseBody = "{\"Name\":\"John\"}";

        mockServer.stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                        .withStatus(200)
                        .withBody(responseBody)));

        Message payload = MessageBuilder.get().build();

        // When
        Message outMessage = component.apply(payload, flowContext);

        // Then
        assertContent(outMessage, responseBody, APPLICATION_JSON);
    }*/
}
