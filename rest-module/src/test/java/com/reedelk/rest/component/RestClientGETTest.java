package com.reedelk.rest.component;

import org.junit.jupiter.api.Tag;

@Tag("integration")
class RestClientGETTest extends RestClientAbstractTest {
/**
    @Test
    void shouldGETExecuteCorrectlyWhenResponse200() throws InterruptedException {
        // Given
        HttpRequest request = request()
                .withMethod(GET.name())
                .withPath("/v1/resource");
        mockServer.when(request)
                .respond(response()
                        .withStatusCode(OK_200.code())
                        .withHeader("Content-Type", MimeType.TEXT.toString())
                        .withBody("Test response body"));

        String path = "/v1/resource";
        String baseURL = "http://localhost:" + PORT;
        RestClient component = componentWith(baseURL, path, GET);

        Message payload = MessageBuilder.get().text("Payload").build();

        // When
        Message outMessage = component.apply(payload);

        // Then
        assertThatContentIs(outMessage, "Test response body");
        assertThatMimeTypeIs(outMessage, MimeType.TEXT);

        mockServer.clear(request, ClearType.ALL);
    }*/
}
