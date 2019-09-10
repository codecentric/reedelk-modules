package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.HttpHeader;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.reedelk.rest.server.mapper.HttpRequestAttribute.*;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class HttpRequestMessageMapperTest {

    private final String matchingPath = "/resource/{id}/group/{group}";
    private HttpRequestMessageMapper mapper = new HttpRequestMessageMapper(matchingPath);

    @Test
    void shouldCorrectlyMapMessageAttributesAndContentCorrectly() {
        // Given
        String queryString = "queryParam1=queryValue1&queryParam2=queryValue2";
        String requestPath = "/resource/34/group/user";
        String requestUri = requestPath + "?" + queryString;

        HashMap<String,String> params = new HashMap<>();
        params.put("param1", "value1");

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(HttpHeader.CONTENT_TYPE, "application/json");
        httpHeaders.add("X-Correlation-ID", "aabbccdd1");

        HttpServerRequest mockRequest = mock(HttpServerRequest.class);
        doReturn(PUT).when(mockRequest).method();
        doReturn(params).when(mockRequest).params();
        doReturn(requestUri).when(mockRequest).uri();
        doReturn(httpHeaders).when(mockRequest).requestHeaders();
        doReturn(ByteBufFlux.fromInbound(Flux.just("body"))).when(mockRequest).receive();
        doReturn(new InetSocketAddress("localhost", 7070)).when(mockRequest).remoteAddress();
        doReturn(HttpVersion.HTTP_1_0).when(mockRequest).version();
        doReturn(HttpScheme.HTTP.toString()).when(mockRequest).scheme();

        // When
        Message message = mapper.map(mockRequest);

        // Then
        HashMap<String, List<String>> expectedQueryParams = new HashMap<>();
        expectedQueryParams.put("queryParam1", Collections.singletonList("queryValue1"));
        expectedQueryParams.put("queryParam2", Collections.singletonList("queryValue2"));

        HashMap<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put(HttpHeader.CONTENT_TYPE, "application/json");
        expectedHeaders.put("X-Correlation-ID", "aabbccdd1");

        assertThatContainsAttribute(message, remoteAddress(), "localhost/127.0.0.1:7070");
        assertThatContainsAttribute(message, matchingPath(), matchingPath);
        assertThatContainsAttribute(message, queryParams(), expectedQueryParams);
        assertThatContainsAttribute(message, requestPath(), requestPath);
        assertThatContainsAttribute(message, requestUri(), requestUri);
        assertThatContainsAttribute(message, queryString(), queryString);
        assertThatContainsAttribute(message, pathParams(), params);
        assertThatContainsAttribute(message, version(), HttpVersion.HTTP_1_0.text());
        assertThatContainsAttribute(message, headers(), expectedHeaders);
        assertThatContainsAttribute(message, scheme(), HttpScheme.HTTP.toString());
        assertThatContainsAttribute(message, method(), PUT.name());

        // Check that the content's mime type is correct
        Type contentType = message.getContent().type();
        MimeType mimeType = contentType.getMimeType();
        assertThat(mimeType).isEqualTo(MimeType.APPLICATION_JSON);
    }

    private void assertThatContainsAttribute(Message message, String attributeName, Serializable attributeValue) {
        MessageAttributes attributes = message.getAttributes();
        assertThat(attributes).containsEntry(attributeName, attributeValue);
    }
}