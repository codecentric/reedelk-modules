package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.HttpHeader;
import com.reedelk.runtime.api.message.content.MimeType;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.netty.handler.codec.http.HttpMethod.PUT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class HttpRequestWrapperTest {

    @Mock
    private HttpServerRequest mockRequest;

    private HttpRequestWrapper wrapper;

    @BeforeEach
    void setUp() {
        wrapper = new HttpRequestWrapper(mockRequest);
    }

    @Test
    void shouldReturnCorrectVersion() {
        // Given
        doReturn(HttpVersion.HTTP_1_1).when(mockRequest).version();

        // When
        String actualVersion = wrapper.version();

        // Then
        assertThat(actualVersion).isEqualTo("HTTP/1.1");
    }

    @Test
    void shouldReturnCorrectScheme() {
        // Given
        doReturn(HttpScheme.HTTPS.toString()).when(mockRequest).scheme();

        // When
        String actualScheme = wrapper.scheme();

        // Then
        assertThat(actualScheme).isEqualTo("https");
    }

    @Test
    void shouldReturnCorrectMethod() {
        // Given
        doReturn(PUT).when(mockRequest).method();

        // When
        String actualMethod = wrapper.method();

        // Then
        assertThat(actualMethod).isEqualTo("PUT");
    }

    @Test
    void shouldReturnCorrectRequestUri() {
        // Given
        String expectedUri = "/resource/34/group/user?queryParam1=queryValue1&queryParam2=queryValue2";
        doReturn(expectedUri).when(mockRequest).uri();

        // When
        String actualUri = wrapper.requestUri();

        // Then
        assertThat(actualUri).isEqualTo(expectedUri);
    }

    @Test
    void shouldReturnCorrectMimeTypeWhenContentTypeHeaderIsSet() {
        // Given
        MimeType expectedMimeType = MimeType.APPLICATION_JSON;

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(HttpHeader.CONTENT_TYPE, "application/json");
        doReturn(httpHeaders).when(mockRequest).requestHeaders();

        // When
        MimeType actualMimeType = wrapper.mimeType();

        // Then
        assertThat(actualMimeType).isEqualTo(expectedMimeType);
    }

    @Test
    void shouldReturnUnknownContentTypeWhenContentTypeHeaderNotSet() {
        // Given
        MimeType expectedMimeType = MimeType.UNKNOWN;

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        doReturn(httpHeaders).when(mockRequest).requestHeaders();

        // When
        MimeType actualMimeType = wrapper.mimeType();

        // Then
        assertThat(actualMimeType).isEqualTo(expectedMimeType);
    }

    @Test
    void shouldReturnCorrectQueryString() {
        // Given
        String expectedQueryString = "queryParam1=queryValue1&queryParam2=queryValue2";

        String uri = "/resource/34/group/user?queryParam1=queryValue1&queryParam2=queryValue2";
        doReturn(uri).when(mockRequest).uri();

        // When
        String actualQueryString = wrapper.queryString();

        // Then
        assertThat(actualQueryString).isEqualTo(expectedQueryString);
    }

    @Test
    void shouldReturnEmptyQueryStringWhenAbsent() {
        // Given
        String expectedQueryString = "";

        String uri = "/resource/34/group/user";
        doReturn(uri).when(mockRequest).uri();

        // When
        String actualQueryString = wrapper.queryString();

        // Then
        assertThat(actualQueryString).isEqualTo(expectedQueryString);
    }

    @Test
    void shouldReturnEmptyQueryStringWhenOnlyQuestionMark() {
        // Given
        String expectedQueryString = "";

        String uri = "/resource/34/group/user?";
        doReturn(uri).when(mockRequest).uri();

        // When
        String actualQueryString = wrapper.queryString();

        // Then
        assertThat(actualQueryString).isEqualTo(expectedQueryString);
    }

    @Test
    void shouldRequestPathReturnPathWithoutQueryParams() {
        // Given
        String expectedRequestPath = "/resource/34/group/user";

        String uri = "/resource/34/group/user?queryParam1=queryValue1&queryParam2=queryValue2";
        doReturn(uri).when(mockRequest).uri();

        // When
        String actualRequestPath = wrapper.requestPath();

        // Then
        assertThat(actualRequestPath).isEqualTo(expectedRequestPath);
    }

    @Test
    void shouldRequestPathReturnOriginalPathWhenNoQueryParams() {
        // Given
        String expectedRequestPath = "/resource/34/group/user";

        String uri = "/resource/34/group/user";
        doReturn(uri).when(mockRequest).uri();

        // When
        String actualRequestPath = wrapper.requestPath();

        // Then
        assertThat(actualRequestPath).isEqualTo(expectedRequestPath);
    }

    @Test
    void shouldReceiverReturnStream() {
        // Given
        ByteBufFlux expectedStream = ByteBufFlux.fromInbound(Flux.just("my test body"));
        doReturn(expectedStream).when(mockRequest).receive();

        // When
        ByteBufFlux actualStream = wrapper.data();

        // Then
        assertThat(actualStream).isEqualTo(expectedStream);
    }

    @Test
    void shouldRemoteAddressReturnCorrectAddress() {
        // Given
        String expectedAddress = "localhost/127.0.0.1:6060";
        InetSocketAddress address = new InetSocketAddress("localhost", 6060);
        doReturn(address).when(mockRequest).remoteAddress();

        // When
        String actualAddress = wrapper.remoteAddress();

        // Then
        assertThat(actualAddress).isEqualTo(expectedAddress);
    }

    @Test
    void shouldQueryParamsReturnCorrectQueryParameters() {
        // Given
        String uri = "/resource/34/group/user?queryParam1=queryValue1&queryParam2=queryValue2";
        doReturn(uri).when(mockRequest).uri();

        // When
        HashMap<String, List<String>> queryParams = wrapper.queryParams();

        // Then
        assertThat(queryParams).containsEntry("queryParam1", singletonList("queryValue1"));
        assertThat(queryParams).containsEntry("queryParam2", singletonList("queryValue2"));
    }

    @Test
    void shouldReturnCorrectPathParams() {
        // Given
        Map<String,String> pathParams = new HashMap<>();
        pathParams.put("param1", "param1Value");
        pathParams.put("param2", "param2Value");
        doReturn(pathParams).when(mockRequest).params();

        // When
        HashMap<String, String> actualParams = wrapper.params();

        // Then
        assertThat(actualParams).containsEntry("param1", "param1Value");
        assertThat(actualParams).containsEntry("param2", "param2Value");
    }

    @Test
    void shouldReturnCorrectRequestHeaders() {
        // Given
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(HttpHeader.CONTENT_TYPE, "text/plain");
        httpHeaders.add("X-Correlation-ID", "aabbcc11ff");
        httpHeaders.add("accept-encoding", "gzip,deflate");

        doReturn(httpHeaders).when(mockRequest).requestHeaders();

        // When
        TreeMap<String, List<String>> headers = wrapper.headers();

        // Then
        assertThat(headers).containsEntry(HttpHeader.CONTENT_TYPE, singletonList("text/plain"));
        assertThat(headers).containsEntry("X-Correlation-ID", singletonList("aabbcc11ff"));
        assertThat(headers).containsEntry("accept-encoding", asList("gzip", "deflate"));
    }
}