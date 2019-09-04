package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

abstract class RestClientAbstractTest {

    static final int PORT = 8787;

    static WireMockServer mockServer;

    static String path = "/v1/resource";


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

    void assertThatMimeTypeIs(Message message, MimeType expectedMimeType) {
        TypedContent<?> typedContent = message.getTypedContent();
        Type type = typedContent.type();
        MimeType mimeType = type.getMimeType();
        assertThat(mimeType).isEqualTo(expectedMimeType);
    }

    void assertThatContentIs(Message message, String expectedContent) {
        TypedContent<?> typedContent = message.getTypedContent();
        assertThat(typedContent.asString()).isEqualTo(expectedContent);
    }

    RestClient componentWith(String baseURL, String path, RestMethod method) {
        RestClient restClient = new RestClient();
        restClient.setBaseUrl(baseURL);
        restClient.setPath(path);
        restClient.setMethod(method);
        return restClient;
    }
}
