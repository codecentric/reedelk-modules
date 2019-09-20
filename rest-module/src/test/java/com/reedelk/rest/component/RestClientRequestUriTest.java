package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.NMapEvaluation;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.RestMethod.valueOf;
import static com.reedelk.rest.utils.TestTag.INTEGRATION;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@Tag(INTEGRATION)
class RestClientRequestUriTest extends RestClientAbstractTest {

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyBuildRequestUriWithPathParams(String method) {
        // Given
        String path = "/resource/{id}/group/{group}";
        String expectedPath = "/resource/aabbccddeeff/group/user";

        Map<String, String> pathParameters = ImmutableMap.of("id", "aabbccddeeff", "group", "user");

        // Expect
        assertExpectedPath(method, path, expectedPath, pathParameters, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyBuildRequestUriWithQueryParams(String method) {
        // Given
        String path = "/resource";
        String expectedPath = "/resource?query1=value1&query2=value2";

        Map<String, String> queryParameters = ImmutableMap.of("query1", "value1", "query2", "value2");

        // Expect
        assertExpectedPath(method, path, expectedPath, null, queryParameters);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyBuildRequestUriWithPathAndQueryParams(String method) {
        // Given
        String path = "/resource/{id}/title/{title}";
        String expectedPath = "/resource/aabb1122/title/manager?query1=value1&query2=value2";

        Map<String, String> queryParameters = ImmutableMap.of("query1", "value1", "query2", "value2");
        Map<String, String> pathParameters = ImmutableMap.of("id", "aabb1122", "title", "manager");

        // Expect
        assertExpectedPath(method, path, expectedPath, pathParameters, queryParameters);
    }

    void assertExpectedPath(String method, String path, String expectedPath, Map<String, String> pathParameters, Map<String, String> queryParameters) {
        // Given
        givenThat(WireMock.any(urlEqualTo(expectedPath))
                .willReturn(aResponse().withStatus(200)));

        Message message = MessageBuilder.get().empty().build();

        // When
        RestMethod restMethod = valueOf(method);
        RestClient component = componentWith(restMethod, baseURL, path);

        configureRequestAndQueryParams(pathParameters, queryParameters, component);

        // Expect
        AssertThatHttpResponseContent
                .isSuccessful(component, message, flowContext);
    }

    private void configureRequestAndQueryParams(Map<String, String> pathParameters, Map<String, String> queryParameters, RestClient component) {
        if (pathParameters != null && queryParameters != null) {
            component.setPathParameters(pathParameters);
            component.setQueryParameters(queryParameters);
            doReturn(new NMapEvaluation<>(asList(pathParameters, queryParameters)))
                    .when(scriptEngine)
                    .evaluate(any(Message.class), any(FlowContext.class), anyMap(), anyMap());
        }
        if (pathParameters != null && queryParameters == null) {
            component.setPathParameters(pathParameters);
            doReturn(new NMapEvaluation<>(singletonList(pathParameters)))
                    .when(scriptEngine)
                    .evaluate(any(Message.class), any(FlowContext.class), anyMap());
        }
        if (pathParameters == null && queryParameters != null) {
            component.setQueryParameters(queryParameters);
            doReturn(new NMapEvaluation<>(singletonList(queryParameters)))
                    .when(scriptEngine)
                    .evaluate(any(Message.class), any(FlowContext.class), anyMap());
        }
    }
}
