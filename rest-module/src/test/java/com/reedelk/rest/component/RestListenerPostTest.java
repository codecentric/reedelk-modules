package com.reedelk.rest.component;

import com.reedelk.rest.configuration.listener.ListenerConfiguration;
import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Part;
import com.reedelk.runtime.api.message.type.Parts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.HttpHeader.TRANSFER_ENCODING;
import static com.reedelk.rest.commons.Messages.RestListener.ERROR_MULTIPART_NOT_SUPPORTED;
import static com.reedelk.rest.commons.RestMethod.POST;
import static com.reedelk.rest.commons.RestMethod.PUT;
import static com.reedelk.runtime.api.message.type.MimeType.*;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

class RestListenerPostTest extends RestListenerAbstractTest {

    private static final String TEST_JSON_BODY = "{\"name\":\"John\"}";
    private static final String TEST_TEXT_BODY = "This is a sample text";


    // TODO: What happens when callback.onResult is never called? There should be a timeout....!??!?
    // TODO: Test SSL and SSL Certificate

    private ListenerConfiguration defaultConfiguration;
    private HttpPost defaultRequest;

    private Message inboundMessage;
    private Object payload;

    @BeforeEach
    void setUp() {
        super.setUp();
        defaultConfiguration = new ListenerConfiguration();
        defaultConfiguration.setHost(DEFAULT_HOST);
        defaultConfiguration.setPort(DEFAULT_PORT);
        defaultRequest = new HttpPost("http://" + DEFAULT_HOST + ":" + DEFAULT_PORT);
    }

    @Test
    void shouldReturn200() {
        // Given
        StringEntity entity = new StringEntity(TEST_JSON_BODY, ContentType.APPLICATION_JSON);
        defaultRequest.setEntity(entity);

        RestListener listener = listenerWith(POST, defaultConfiguration);
        listener.addEventListener((message, callback) -> callback.onResult(message, context));
        listener.onStart();

        // Expect
        assertStatusCodeIs(defaultRequest, SC_OK);
    }

    @Test
    void shouldPostJsonBody() throws IOException {
        // Given
        StringEntity entity = new StringEntity(TEST_JSON_BODY, ContentType.APPLICATION_JSON);
        defaultRequest.setEntity(entity);

        RestListener listener = listenerWith(POST, defaultConfiguration);

        // Expect
        assertPostBody(listener, defaultRequest, TEST_JSON_BODY, APPLICATION_JSON);
    }

    @Test
    void shouldPostTextBody() throws IOException {
        // Given
        StringEntity entity = new StringEntity(TEST_TEXT_BODY, ContentType.TEXT_PLAIN);
        defaultRequest.setEntity(entity);

        RestListener listener = listenerWith(POST, defaultConfiguration);

        // Expect
        assertPostBody(listener, defaultRequest, TEST_TEXT_BODY, TEXT);
    }

    @Test
    void shouldPostBinaryBody() throws IOException {
        // Given
        byte[] binaryData = TEST_JSON_BODY.getBytes();
        ByteArrayEntity entity = new ByteArrayEntity(binaryData, ContentType.DEFAULT_BINARY);
        defaultRequest.setEntity(entity);

        RestListener listener = listenerWith(POST, defaultConfiguration);

        // Expect
        assertPostBody(listener, defaultRequest, binaryData, BINARY);
    }

    @Test
    void shouldPostUrlEncodedFormEntity() throws IOException {
        // Given
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", "John"));
        params.add(new BasicNameValuePair("password", "pass"));
        defaultRequest.setEntity(new UrlEncodedFormEntity(params));

        RestListener listener = listenerWith(POST, defaultConfiguration);

        // Expect
        assertPostBody(listener, defaultRequest,
                "username=John&password=pass",
                APPLICATION_FORM_URL_ENCODED);
    }

    @Test
    void shouldPostMultipartData() throws IOException {
        // Given
        byte[] binaryContent = "my binary text".getBytes();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("username", "John");
        builder.addBinaryBody("myfile", binaryContent, ContentType.APPLICATION_OCTET_STREAM, "file.ext");

        HttpEntity multipart = builder.build();
        defaultRequest.setEntity(multipart);

        RestListener listener = listenerWith(POST, defaultConfiguration);

        // When
        makeRequest(listener, defaultRequest);

        // Then
        Parts parts = (Parts) payload;

        assertThat(parts).containsOnlyKeys("username", "myfile");
        assertExistsPartWith(parts, "username", TEXT, "John", Collections.emptyMap());
        assertExistsPartWith(parts, "myfile", BINARY, binaryContent,
                ImmutableMap.of(
                        CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.toString(),
                        TRANSFER_ENCODING, "binary",
                        "filename", "file.ext"));
    }

    // Multipart is ONLY supported for requests with method POST and HTTP 1.1.
    @Test
    void shouldPutMultipartReturnInternalServerError() throws IOException {
        // Given
        HttpPut request = new HttpPut("http://" + DEFAULT_HOST + ":" + DEFAULT_PORT);

        byte[] binaryContent = "my binary text".getBytes();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("username", "John");
        builder.addBinaryBody("myfile", binaryContent, ContentType.APPLICATION_OCTET_STREAM, "file.ext");

        HttpEntity multipart = builder.build();
        request.setEntity(multipart);

        RestListener listener = listenerWith(PUT, defaultConfiguration);
        listener.onStart();

        // When
        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(SC_INTERNAL_SERVER_ERROR);
        String responseAsString = EntityUtils.toString(response.getEntity());
        assertThat(responseAsString).contains(ERROR_MULTIPART_NOT_SUPPORTED.format());
    }

    private void assertExistsPartWith(Parts parts, String name, MimeType mimeType, Object data, Map<String,String> attributes) {
        Part usernamePart = parts.get(name);
        assertThat(usernamePart.getName()).isEqualTo(name);
        assertThat(usernamePart.getContent().type().getMimeType()).isEqualTo(mimeType);
        assertThat(usernamePart.getContent().data()).isEqualTo(data);
        assertThat(usernamePart.getAttributes()).isEqualTo(attributes);
    }

    private void assertPostBody(RestListener listener, HttpPost request, Object expectedContent, MimeType expectedMimeType) throws IOException {
        // Execute request
        makeRequest(listener, request);

        // Assertions
        assertThat(payload).isEqualTo(expectedContent);
        assertThat(inboundMessage.getContent().type().getMimeType()).isEqualTo(expectedMimeType);
    }

    private void makeRequest(RestListener listener, HttpPost request) throws IOException {
        // Setup event listener and start route
        listener.addEventListener((message, callback) ->
                new Thread(() -> {
                    // We must consume the payload in this Thread. Because the
                    // Thread calling the callback is a NIO Thread, hence we would
                    // not be able to consume the payload because it is a Fast non-blocking Thread.
                    inboundMessage = message;
                    payload = message.payload();
                    callback.onResult(MessageBuilder.get().empty().build(), context);
                }).start());
        listener.onStart();

        // Execute http request
        HttpClientBuilder.create().build().execute(request);
    }
}
