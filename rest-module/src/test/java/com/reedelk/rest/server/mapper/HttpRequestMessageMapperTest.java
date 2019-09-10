package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;

import java.util.HashMap;

import static com.reedelk.rest.server.mapper.HttpRequestAttribute.method;
import static com.reedelk.rest.server.mapper.HttpRequestAttribute.path;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class HttpRequestMessageMapperTest {

    private HttpRequestMessageMapper mapper = new HttpRequestMessageMapper();

    @Test
    void shouldCorrectlyMapMessageAttributes() {
        // Given
        String requestPath = "/resource/34/group/user";
        String requestUri = requestPath + "?queryParam1=queryValue1&queryParam2=queryValue2";

        HashMap<String,String> params = new HashMap<>();
        params.put("param1", "value1");

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add("content-type", "application/json");

        HttpServerRequest mockRequest = mock(HttpServerRequest.class);
        doReturn(PUT).when(mockRequest).method();
        doReturn(params).when(mockRequest).params();
        doReturn(requestUri).when(mockRequest).uri();
        doReturn(httpHeaders).when(mockRequest).requestHeaders();
        doReturn(ByteBufFlux.fromInbound(Flux.just("body"))).when(mockRequest).receive();

        // When
        Message message = mapper.map(mockRequest);

        // Then
        assertThatContentMimeTypeIs(message, MimeType.APPLICATION_JSON);
        assertThatContainsAttribute(message, method(), "PUT");
        assertThatContainsAttribute(message, path(), requestPath);
        assertThatContainsAttribute();



        /**
         *     static String path() {
         *         return "path";
         *     }

         *
         *     static String headers() {
         *         return "headers";
         *     }
         *
         *     static String pathParams() {
         *         return "pathParams";
         *     }
         *
         *     static String queryParams() {
         *         return "queryParams";
         *     }
         */
    }

    private void assertThatContainsAttribute(Message message, String attributeName, String attributeValue) {
        MessageAttributes attributes = message.getAttributes();
        assertThat(attributes).containsEntry(attributeName,attributeValue);
    }

    private void assertThatContentMimeTypeIs(Message message, MimeType expectedMimeType) {
        Type contentType = message.getContent().type();
        MimeType mimeType = contentType.getMimeType();
        assertThat(mimeType).isEqualTo(expectedMimeType);
    }
}