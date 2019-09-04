package com.reedelk.rest.component;

class RestClientPostTest extends RestClientAbstractTest {
/**
    @Test
    void shouldPOSTExecuteCorrectlyWhenResponse200() {
        // Given
        HttpRequest request = request()
                .withMethod(POST.name())
                .withBody("my POST test body")
                .withPath("/v1/resource");

        mockServer.when(request)
                .respond(response()
                        .withStatusCode(OK_200.code())
                        .withHeader("Content-Type", MimeType.TEXT.toString())
                        .withBody("POST was successful"));

        String path = "/v1/resource";
        String baseURL = "http://localhost:" + PORT;
        RestClient component = componentWith(baseURL, path, POST);

        Message payload = MessageBuilder.get().text("my POST test body").build();

        // When
        Message outMessage = component.apply(payload);

        // Then
        assertThatContentIs(outMessage, "POST was successful");
        assertThatMimeTypeIs(outMessage, MimeType.TEXT);

        mockServer.clear(request, ClearType.ALL);
    }
*/
}
