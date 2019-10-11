package com.reedelk.rest.component;

import org.apache.http.client.methods.HttpOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.reedelk.rest.commons.RestMethod.OPTIONS;
import static org.apache.http.HttpStatus.SC_OK;

class RestListenerOptionsTest extends RestListenerAbstractTest {

    private HttpOptions optionsRequest;

    @BeforeEach
    void setUp() {
        super.setUp();
        optionsRequest = new HttpOptions("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT);
    }

    @Test
    void shouldReturn200() {
        // Given
        RestListener listener = listenerWith(OPTIONS, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        // Expect
        assertStatusCodeIs(optionsRequest, SC_OK);
    }
}
