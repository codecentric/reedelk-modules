package com.reedelk.rest.component;

import com.reedelk.rest.configuration.listener.ListenerConfiguration;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;

import static com.reedelk.rest.commons.RestMethod.GET;
import static org.apache.http.HttpStatus.SC_OK;

class RestListenerDefaultTest extends RestListenerAbstractTest {

    @Test
    void shouldUseDefaultHostAndPortWhenConfigDoesNotDefineHostAndPort() {
        // Given
        ListenerConfiguration configWithoutHostAndPortDefined = new ListenerConfiguration();

        int defaultPort = 8080;
        String defaultHost = "localhost";

        HttpGet request = new HttpGet("http://" + defaultHost + ":"+ defaultPort);

        RestListener listener = listenerWith(GET, configWithoutHostAndPortDefined);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        // Expect
        assertStatusCodeIs(request, SC_OK);
    }
}
