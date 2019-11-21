package com.reedelk.rest.component;

import com.reedelk.rest.configuration.listener.ErrorResponse;
import com.reedelk.rest.configuration.listener.ListenerConfiguration;
import com.reedelk.rest.configuration.listener.Response;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

import static com.reedelk.rest.commons.RestMethod.GET;
import static com.reedelk.rest.server.mapper.HttpRequestAttribute.*;
import static com.reedelk.runtime.api.commons.ImmutableMap.of;
import static com.reedelk.runtime.api.commons.StringUtils.EMPTY;
import static com.reedelk.runtime.api.message.content.MimeType.APPLICATION_JSON;
import static com.reedelk.runtime.api.message.content.MimeType.UNKNOWN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

class RestListenerGetTest extends RestListenerAbstractTest {

    private HttpGet getRequest;

    @BeforeEach
    void setUp() {
        super.setUp();
        getRequest = new HttpGet("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT);
    }

    @Test
    void shouldReturn200WhenPathIsNull() {
        // Given
        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        // Expect
        assertStatusCodeIs(getRequest, SC_OK);
    }

    @Test
    void shouldReturn200WhenPathIsNullAndRequestEndsWithRoot() {
        // Given
        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        HttpUriRequest request = new HttpGet("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT + "/");

        // Expect
        assertStatusCodeIs(request, SC_OK);
    }

    @Test
    void shouldReturn200WhenPathIsRoot() {
        // Given
        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.setPath("/");
        listener.onStart();

        // Expect
        assertStatusCodeIs(getRequest, SC_OK);
    }

    @Test
    void shouldReturn200WhenPathIsRootAndRequestEndsWithRoot() {
        // Given
        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.setPath("/");
        listener.onStart();

        HttpUriRequest request = new HttpGet("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT + "/");

        // Expect
        assertStatusCodeIs(request, SC_OK);
    }

    @Test
    void shouldReturn200WhenPathDefinedWithRegularExpression() {
        // Given
        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener(((message, callback) -> callback.onResult(message, context)));
        listener.setPath("/web/{page:.*}");
        listener.onStart();

        HttpUriRequest request = new HttpGet("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT + "/web/assets/javascript/home.js");

        // Expect
        assertStatusCodeIs(request, SC_OK);
    }

    @Test
    void shouldReturn200WhenBasePathOnlyIsDefined() {
        // Given
        ListenerConfiguration configWithBasePath = new ListenerConfiguration();
        configWithBasePath.setHost(DEFAULT_HOST);
        configWithBasePath.setPort(DEFAULT_PORT);
        configWithBasePath.setBasePath("/api/internal");

        RestListener listener = listenerWith(GET, configWithBasePath);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        HttpUriRequest request = new HttpGet("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT + "/api/internal");

        // Expect
        assertStatusCodeIs(request, SC_OK);
    }

    @Test
    void shouldReturn200WhenBasePathAndPathAreBothDefined() {
        // Given
        ListenerConfiguration configWithBasePath = new ListenerConfiguration();
        configWithBasePath.setHost(DEFAULT_HOST);
        configWithBasePath.setPort(DEFAULT_PORT);
        configWithBasePath.setBasePath("/api/internal");

        RestListener listener = listenerWith(GET, configWithBasePath);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.setPath("/group/{groupId}");
        listener.onStart();

        HttpUriRequest request = new HttpGet("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT + "/api/internal/group/managers");

        // Expect
        assertStatusCodeIs(request, SC_OK);
    }

    @Test
    void shouldReturn404() {
        // Given
        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        HttpUriRequest request = new HttpGet("http://localhost:8881/api");

        // Expect
        assertStatusCodeIs(request, SC_NOT_FOUND);
    }

    @Test
    void shouldReturn500() {
        // Given
        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onError(new IllegalStateException("flow error"), context));
        listener.onStart();

        // Expect
        assertStatusCodeIs(getRequest, SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnEmptyResponseContent() throws IOException {
        // Given
        Message emptyMessage = MessageBuilder.get().empty().build();

        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(emptyMessage, context));
        listener.onStart();

        // Expect
        assertContentIs(getRequest, EMPTY);
    }

    @Test
    void shouldReturnErrorResponseContent() throws IOException {
        // Given
        String errorMessage = "my error";
        DynamicByteArray errorResponseBody = DynamicByteArray.from("#[error]", testModuleId);
        IllegalStateException exception = new IllegalStateException(errorMessage);

        // Status
        doReturn(Optional.empty())
                .when(scriptEngine)
                .evaluate(null, context, exception);

        // Evaluation of error message
        doReturn(Optional.of(errorMessage.getBytes()))
                .when(scriptEngine)
                .evaluate(errorResponseBody, context, exception);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(errorResponseBody);

        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onError(exception, context));
        listener.setErrorResponse(errorResponse);
        listener.onStart();

        // Expect
        assertContentIs(getRequest, errorMessage);
    }

    @Test
    void shouldReturnCorrectContentType() throws IOException {
        // Given
        String json = "{\"name\":\"John\"}";
        Message responseMessage = MessageBuilder.get().json(json).build();

        DynamicByteArray responseBody = DynamicByteArray.from("#[message.payload()]", testModuleId);
        Response listenerResponse = new Response();
        listenerResponse.setBody(responseBody);

        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.setResponse(listenerResponse);
        listener.addEventListener((message, callback) -> callback.onResult(responseMessage, context));
        listener.onStart();

        // When
        HttpResponse response = HttpClientBuilder.create().build().execute(getRequest);

        // Then
        assertContentTypeIs(response, APPLICATION_JSON.toString());
    }

    @Test
    void shouldSetMimeTypeUnknownForInboundMessage() throws IOException {
        // Given
        RestListener listener = listenerWith(GET, defaultConfiguration);
        listener.addEventListener((message, callback) -> {
            inboundMessage = message;
            callback.onResult(message, context);
        });
        listener.onStart();

        // When
        HttpClientBuilder.create().build().execute(getRequest);

        // Then
        MimeType inboundMessageMimeType = inboundMessage.getContent().mimeType();
        assertThat(inboundMessageMimeType).isEqualTo(UNKNOWN);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCorrectlyMapHttpRequestToInboundMessageAttributes() throws IOException {
        // Given
        ListenerConfiguration configWithBasePath = new ListenerConfiguration();
        configWithBasePath.setHost(DEFAULT_HOST);
        configWithBasePath.setPort(DEFAULT_PORT);
        configWithBasePath.setBasePath("/api/internal");

        RestListener listener = listenerWith(GET, configWithBasePath);
        listener.setPath("/group/{groupId}");
        listener.addEventListener((message, callback) -> {
            inboundMessage = message;
            callback.onResult(message, context);
        });
        listener.onStart();

        HttpUriRequest request = new HttpGet("http://" + DEFAULT_HOST + ":"+ DEFAULT_PORT + "/api/internal/group/managers?query1=value1&query2=value2&query2=value3");

        // When
        HttpClientBuilder.create().build().execute(request);

        // Then
        MessageAttributes attributes = inboundMessage.getAttributes();
        assertThat(attributes).isNotNull();

        assertThatAttributeIsEqualTo(attributes, HEADERS, headers -> {
            Map<String, List<String>> headersMap = (Map<String, List<String>>) headers;
            assertThat(headersMap).containsEntry("Accept-Encoding", Arrays.asList("gzip", "deflate"));
            assertThat(headersMap).containsEntry("Connection", singletonList("Keep-Alive"));
            assertThat(headersMap).containsEntry("Host", singletonList("localhost:8881"));
        });
        assertThatAttributeIsEqualTo(attributes, MATCHING_PATH, "/group/{groupId}");
        assertThatAttributeIsEqualTo(attributes, METHOD, "GET");
        assertThatAttributeIsEqualTo(attributes, PATH_PARAMS, new TreeMap<>(of("groupId", "managers")));
        assertThatAttributeIsEqualTo(attributes, QUERY_PARAMS, new TreeMap<>(of("query1", singletonList("value1"), "query2", asList("value2", "value3"))));
        assertThatAttributeIsEqualTo(attributes, QUERY_STRING, "query1=value1&query2=value2&query2=value3");
        assertThatAttributeIsEqualTo(attributes, REQUEST_PATH, "/api/internal/group/managers");
        assertThatAttributeIsEqualTo(attributes, REQUEST_URI, "/api/internal/group/managers?query1=value1&query2=value2&query2=value3");
        assertThatAttributeIsEqualTo(attributes, SCHEME, "http");
        assertThatAttributeIsEqualTo(attributes, VERSION, "HTTP/1.1");

        String remoteAddress = attributes.get(REMOTE_ADDRESS);
        assertThat(remoteAddress).startsWith("/127.0.0.1");
    }

    private void assertThatAttributeIsEqualTo(MessageAttributes attributes, String attributeKey, Serializable expectedValue) {
        assertThatAttributeIsEqualTo(attributes, attributeKey, attributeValue ->
                assertThat(attributeValue).isEqualTo(expectedValue));
    }

    private void assertThatAttributeIsEqualTo(MessageAttributes attributes, String attributeKey, Consumer<Serializable> matcher) {
        Serializable attributeValue = attributes.get(attributeKey);
        matcher.accept(attributeValue);
    }
}
