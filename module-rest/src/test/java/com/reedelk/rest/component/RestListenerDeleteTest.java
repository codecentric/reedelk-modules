package com.reedelk.rest.component;

import org.apache.http.client.methods.HttpDelete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.reedelk.rest.commons.RestMethod.DELETE;
import static org.apache.http.HttpStatus.SC_OK;

class RestListenerDeleteTest extends RestListenerAbstractTest {

    private HttpDelete deleteRequest;

    @BeforeEach
    void setUp() {
        super.setUp();
        deleteRequest = new HttpDelete("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT);
    }

    @Test
    void shouldReturn200() {
        // Given
        RestListener listener = listenerWith(DELETE, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        // Expect
        assertStatusCodeIs(deleteRequest, SC_OK);
    }
}
