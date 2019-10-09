package com.reedelk.rest.component;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.listener.ErrorResponse;
import com.reedelk.rest.configuration.listener.ListenerConfiguration;
import com.reedelk.rest.configuration.listener.Response;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.apache.http.HttpStatus.*;
import static org.mockito.Mockito.doReturn;

class RestListenerGetTest extends RestListenerAbstractTest {

    private static final int DEFAULT_PORT = 8881;
    private static final String DEFAULT_HOST = "localhost";

    private ListenerConfiguration defaultConfiguration;
    private HttpUriRequest defaultRequest;

    @BeforeEach
    void setUp() {
        super.setUp();
        defaultConfiguration = new ListenerConfiguration();
        defaultConfiguration.setHost(DEFAULT_HOST);
        defaultConfiguration.setPort(DEFAULT_PORT);
        defaultRequest = new HttpGet("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT + "/");
    }

    @Test
    void shouldReturn200OK() throws IOException {
        // Given
        RestListener listener = listenerWith(RestMethod.GET, "/", defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        // Given
        HttpResponse response = HttpClientBuilder.create().build().execute(defaultRequest);

        // Then
        assertThatStatusCodeIs(response, SC_OK);
        assertThatContentIs(response, StringUtils.EMPTY);
    }

    @Test
    void shouldReturn500InternalServerErrorWithoutErrorBody() throws IOException {
        // Given
        RestListener listener = listenerWith(RestMethod.GET, "/", defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onError(new IllegalStateException("flow error"), context));
        listener.onStart();

        // Given
        HttpResponse response = HttpClientBuilder.create().build().execute(defaultRequest);

        // Then
        assertThatStatusCodeIs(response, SC_INTERNAL_SERVER_ERROR);
        assertThatContentIs(response, StringUtils.EMPTY);
    }

    @Test
    void shouldReturn500InternalServerErrorWithErrorBody() throws IOException {
        // Given
        String errorMessage = "my error";
        DynamicByteArray responseBody = DynamicByteArray.from("#[error]");
        IllegalStateException exception = new IllegalStateException(errorMessage);

        doReturn(TypedPublisher.fromByteArray(Flux.just(errorMessage.getBytes())))
                .when(scriptEngine)
                .evaluateStream(responseBody, exception, context);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(responseBody);

        RestListener listener = listenerWith(RestMethod.GET, "/", defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onError(exception, context));
        listener.setErrorResponse(errorResponse);
        listener.onStart();

        // Given
        HttpResponse response = HttpClientBuilder.create().build().execute(defaultRequest);

        // Then
        assertThatStatusCodeIs(response, SC_INTERNAL_SERVER_ERROR);
        assertThatContentIs(response, errorMessage);
    }

    @Test
    void shouldReturn404NotFound() throws IOException {
        // Given
        RestListener listener = listenerWith(RestMethod.GET, "/", defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        HttpUriRequest request = new HttpGet("http://localhost:8881/api");

        // Given
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThatStatusCodeIs(response, SC_NOT_FOUND);
        assertThatContentIs(response, StringUtils.EMPTY);
    }

    @Test
    void shouldReturnCorrectContentType() throws IOException {
        // Given
        String json = "{\"name\":\"John\"}";
        Message responseMessage = MessageBuilder.get().json(json).build();

        DynamicByteArray responseBody = DynamicByteArray.from("#[message.payload()]");
        Response listenerResponse = new Response();
        listenerResponse.setBody(responseBody);

        RestListener listener = listenerWith(RestMethod.GET, "/", defaultConfiguration);
        listener.setResponse(listenerResponse);
        listener.addEventListener((message, callback) -> callback.onResult(responseMessage, context));

        doReturn(TypedPublisher.fromByteArray(Mono.just(json.getBytes())))
                .when(scriptEngine)
                .evaluateStream(responseBody, responseMessage, context);
        listener.onStart();

        // Given
        HttpResponse response = HttpClientBuilder.create().build().execute(defaultRequest);

        // Then
        assertThatContentType(response, MimeType.APPLICATION_JSON.toString());
    }
}
