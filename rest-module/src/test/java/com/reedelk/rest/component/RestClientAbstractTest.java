package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

abstract class RestClientAbstractTest {

    protected FlowContext flowContext;

    private static final int PORT = 8787;

    static WireMockServer mockServer;

    static String path = "/v1/resource";
    static String baseURL = "http://localhost:" + PORT;


    @BeforeAll
    static void setUpBeforeAll(){
        mockServer = new WireMockServer(PORT);
        mockServer.start();
    }

    @AfterAll
    static void tearDownAfterAll() {
        mockServer.stop();
    }

    @BeforeEach
    void setUp() {
        mockServer.resetAll();
    }

    void assertContentIs(Message message, String expectedContent, MimeType expectedMimeType) {
        TypedContent<?> typedContent = message.getContent();
        assertThat(typedContent.asString()).isEqualTo(expectedContent);

        Type type = typedContent.type();
        MimeType mimeType = type.getMimeType();
        assertThat(mimeType).isEqualTo(expectedMimeType);
    }

    RestClient componentWith(String baseURL, String path, RestMethod method) {
        RestClient restClient = new RestClient();
        restClient.setBaseUrl(baseURL);
        restClient.setPath(path);
        restClient.setMethod(method);
        return restClient;
    }
}
