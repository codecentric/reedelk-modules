package com.reedelk.rest.component;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.reedelk.rest.commons.RestMethod.PUT;
import static com.reedelk.runtime.api.message.content.MimeType.*;
import static org.apache.http.HttpStatus.SC_OK;

class RestListenerPutTest extends RestListenerAbstractTest {

    private HttpPut putRequest;

    @BeforeEach
    void setUp() {
        super.setUp();
        putRequest = new HttpPut("http://" + DEFAULT_HOST + ":" + DEFAULT_PORT);
    }

    @Test
    void shouldReturn200() {
        // Given
        StringEntity entity = new StringEntity(TEST_JSON_BODY, ContentType.APPLICATION_JSON);
        putRequest.setEntity(entity);

        RestListener listener = listenerWith(PUT, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        // Expect
        assertStatusCodeIs(putRequest, SC_OK);
    }

    @Test
    void shouldPutJsonBody() throws IOException {
        // Given
        StringEntity entity = new StringEntity(TEST_JSON_BODY, ContentType.APPLICATION_JSON);
        putRequest.setEntity(entity);

        RestListener listener = listenerWith(PUT, defaultConfiguration);

        // Expect
        assertBodySent(listener, putRequest, TEST_JSON_BODY, APPLICATION_JSON);
    }

    @Test
    void shouldPutTextBody() throws IOException {
        // Given
        StringEntity entity = new StringEntity(TEST_TEXT_BODY, ContentType.TEXT_PLAIN);
        putRequest.setEntity(entity);

        RestListener listener = listenerWith(PUT, defaultConfiguration);

        // Expect
        assertBodySent(listener, putRequest, TEST_TEXT_BODY, TEXT);
    }

    @Test
    void shouldPostBinaryBody() throws IOException {
        // Given
        byte[] binaryData = TEST_JSON_BODY.getBytes();
        ByteArrayEntity entity = new ByteArrayEntity(binaryData, ContentType.DEFAULT_BINARY);
        putRequest.setEntity(entity);

        RestListener listener = listenerWith(PUT, defaultConfiguration);

        // Expect
        assertBodySent(listener, putRequest, binaryData, BINARY);
    }
}
