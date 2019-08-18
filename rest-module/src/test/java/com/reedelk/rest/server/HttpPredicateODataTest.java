package com.reedelk.rest.server;

import com.reedelk.rest.commons.AsSerializableMap;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.netty.http.server.HttpServerRequest;

import java.util.HashMap;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.assertj.core.api.Assertions.assertThat;

class HttpPredicateODataTest {

    // GET /People
    @Test
    void requestEntityCollections() {
        // Given
        HttpServerRequest request = requestWith(GET, "/People");
        HttpPredicate predicate = new HttpPredicate("/People", HTTP_1_1, GET);

        // When
        boolean matches = predicate.test(request);
        HashMap<String, ?> pathParams = AsSerializableMap.of(predicate.apply(request.uri()));

        // Then
        assertThat(matches).isTrue();
        assertThat(pathParams).isEmpty();
    }

    // GET /People('russellwhyte')
    @Test
    void requestIndividualEntityByID() {
        // Given
        HttpServerRequest request = requestWith(GET, "/People('russellwhyte')");
        HttpPredicate predicate = new HttpPredicate("/People('{ID}')", HTTP_1_1, GET);


        // When
        boolean matches = predicate.test(request);
        HashMap<String, ?> pathParams = AsSerializableMap.of(predicate.apply(request.uri()));


        // Then
        assertThat(matches).isTrue();
        assertThat(pathParams).containsKeys("ID");
        assertThat(pathParams.get("ID")).isEqualTo("russellwhyte");
    }

    private HttpServerRequest requestWith(HttpMethod method, String uri) {
        HttpServerRequest mockRequest = Mockito.mock(HttpServerRequest.class);
        Mockito.doReturn(HTTP_1_1).when(mockRequest).version();
        Mockito.doReturn(method).when(mockRequest).method();
        Mockito.doReturn(uri).when(mockRequest).uri();
        return mockRequest;
    }

}