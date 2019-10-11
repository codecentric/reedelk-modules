package com.reedelk.rest.component;

import org.apache.http.client.methods.HttpHead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.reedelk.rest.commons.RestMethod.HEAD;
import static org.apache.http.HttpStatus.SC_OK;

class RestListenerHeadTest extends RestListenerAbstractTest {

    private HttpHead headRequest;

    @BeforeEach
    void setUp() {
        super.setUp();
        headRequest = new HttpHead("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT);
    }

    @Test
    void shouldReturn200() {
        // Given
        RestListener listener = listenerWith(HEAD, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        // Expect
        assertStatusCodeIs(headRequest, SC_OK);
    }
}
