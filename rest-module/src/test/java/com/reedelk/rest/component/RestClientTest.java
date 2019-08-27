package com.reedelk.rest.component;

import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static com.reedelk.rest.configuration.RestMethod.GET;
import static com.reedelk.rest.configuration.RestMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;

@Tag("integration")
class RestClientTest {

    private static final int PORT = 8787;

    private static ClientAndServer mockServer;

    private static MockServerClient client;

    @BeforeAll
    static void setUpBeforeAll(){
        mockServer = ClientAndServer.startClientAndServer(PORT);
        client = new MockServerClient("localhost", PORT);
    }

    @AfterAll
    static void tearDownAfterAll() {
        mockServer.stop();
        client.stop();
    }

    @Test
    void shouldAssignContentMimeTypeFromResponseContentType() throws InterruptedException {
        // Given
        client.when(request()
                .withMethod(GET.name())
                .withPath("/v1/resource"), exactly(1))
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
    }

    @Test
    void shouldSendCorrectPayloadWhenRequestIsPost() {
        // Given
        client.when(request()
                .withMethod(POST.name())
                .withBody("my test body")
                .withPath("/v1/resource"), exactly(1))
                .respond(response()
                        .withStatusCode(OK_200.code())
                        .withHeader("Content-Type", MimeType.TEXT.toString())
                        .withBody("POST was successful"));

        String path = "/v1/resource";
        String baseURL = "http://localhost:" + PORT;
        RestClient component = componentWith(baseURL, path, POST);

        Message payload = MessageBuilder.get().text("my test body").build();

        // When
        Message outMessage = component.apply(payload);

        // Then
        assertThatContentIs(outMessage, "POST was successful");
        assertThatMimeTypeIs(outMessage, MimeType.TEXT);
    }

    private void assertThatMimeTypeIs(Message message, MimeType expectedMimeType) {
        TypedContent<?> typedContent = message.getTypedContent();
        Type type = typedContent.type();
        MimeType mimeType = type.getMimeType();
        assertThat(mimeType).isEqualTo(expectedMimeType);
    }

    private void assertThatContentIs(Message message, String expectedContent) {
        TypedContent<?> typedContent = message.getTypedContent();
        assertThat(typedContent.asString()).isEqualTo(expectedContent);
    }

    private RestClient componentWith(String baseURL, String path, RestMethod method) {
        RestClient restClient = new RestClient();
        restClient.setBaseUrl(baseURL);
        restClient.setPath(path);
        restClient.setMethod(method);
        return restClient;
    }
}