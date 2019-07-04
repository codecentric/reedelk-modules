package com.esb.rest.server.route;

import com.esb.rest.commons.RestMethod;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.esb.rest.commons.RestMethod.*;
import static org.assertj.core.api.Assertions.assertThat;

class RouteTest {

    private RouteHandler testHandler = request -> request;

    @Test
    void shouldMatchCorrectlyMethodAndPath() {
        // Given
        Route customers = new Route(POST, "/customers/{id}/{group}", testHandler);

        // When
        boolean matches = customers.matches(POST, "/customers/843/workers");

        // Then
        assertThat(matches).isTrue();
    }

    @Test
    void shouldBindPathParametersCorrectly() {
        // Given
        Route customers = new Route(GET, "/customers/{id}/{group}", testHandler);

        // When
        Map<String, String> pathParamsMap = customers.bindPathParams("/customers/84234/managers");

        // Then
        assertThat(pathParamsMap).containsKeys("id", "group");
        assertThat(pathParamsMap.get("id")).isEqualTo("84234");
        assertThat(pathParamsMap.get("group")).isEqualTo("managers");
    }

    @Test
    void shouldReturnCorrectRouteHandler() {
        // Given
        Route test = new Route(DELETE, "/test", testHandler);

        // When
        RouteHandler handler = test.handler();

        // Then
        assertThat(handler).isEqualTo(testHandler);
    }

    @Test
    void shouldReturnCorrectMethod() {
        // Given
        Route test = new Route(OPTIONS, "/test", testHandler);

        // When
        RestMethod method = test.getMethod();

        // Then
        assertThat(method).isEqualTo(OPTIONS);
    }
}
