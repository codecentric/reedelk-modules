package com.reedelk.rest.component;

import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Tag("integration")
class RestClientTest {

    private static final int PORT = 8787;

    private static ClientAndServer mockServer;

    private RestClient component;

    @BeforeAll
    static void setUpBeforeAll(){
        mockServer = ClientAndServer.startClientAndServer(PORT);
    }

    @AfterAll
    static void tearDownAfterAll() {
        mockServer.stop();
    }

    @BeforeEach
    void setUp() {
        component = new RestClient();
    }

    @Test
    void shouldAssignContentMimeTypeFromResponseContentType() throws InterruptedException {
        // Given
        MockServerClient client = new MockServerClient("localhost", PORT);
        client.when(request()
                .withMethod("GET")
                .withPath("/v1/resource"), exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Test response body"));

        Message payload = MessageBuilder.get().text("Payload").build();
        component.setBaseUrl("http://localhost:8787");
        component.setPath("/v1/resource");
        component.setMethod(RestMethod.GET);

        // When
        Message out = component.apply(payload);

        // Then
        TypedContent<?> typedContent = out.getTypedContent();
        Type type = typedContent.type();
        MimeType mimeType = type.getMimeType();

        assertThat(typedContent.asString()).isEqualTo("Test response body");
        assertThat(mimeType).isEqualTo(MimeType.TEXT);

        client.stop();
    }
}