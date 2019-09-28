package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.DynamicMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.RestMethod.valueOf;
import static com.reedelk.runtime.api.commons.ImmutableMap.of;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;


class RestClientRequestUriTest extends RestClientAbstractTest {

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyBuildRequestUriWithPathParams(String method) {
        // Given
        String path = "/resource/{id}/group/{group}";
        String expectedPath = "/resource/aabbccddeeff/group/user";

        DynamicMap<String> pathParameters = DynamicMap.from(of(
                "id", "aabbccddeeff",
                "group", "user"));

        // Expect
        assertExpectedPath(method, path, expectedPath, pathParameters, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyBuildRequestUriWithQueryParams(String method) {
        // Given
        String path = "/resource";
        String expectedPath = "/resource?query1=value1&query2=value2";

        DynamicMap<String> queryParameters = DynamicMap.from(of(
                "query1", "value1",
                "query2", "value2"));

        // Expect
        assertExpectedPath(method, path, expectedPath, null, queryParameters);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyBuildRequestUriWithPathAndQueryParams(String method) {
        // Given
        String path = "/resource/{id}/title/{title}";
        String expectedPath = "/resource/aabb1122/title/manager?query1=value1&query2=value2";

        DynamicMap<String> queryParameters = DynamicMap.from(of("query1", "value1", "query2", "value2"));
        DynamicMap<String> pathParameters = DynamicMap.from(of("id", "aabb1122", "title", "manager"));

        // Expect
        assertExpectedPath(method, path, expectedPath, pathParameters, queryParameters);
    }

    void assertExpectedPath(String method, String path, String expectedPath, DynamicMap<String> pathParameters, DynamicMap<String> queryParameters) {
        // Given
        givenThat(WireMock.any(urlEqualTo(expectedPath))
                .willReturn(aResponse().withStatus(200)));

        Message message = MessageBuilder.get().empty().build();

        // When
        RestMethod restMethod = valueOf(method);
        RestClient component = componentWith(restMethod, baseURL, path);

        configureRequestAndQueryParams(component, pathParameters, queryParameters);

        // Expect
        AssertHttpResponse
                .isSuccessful(component, message, flowContext);
    }

    private void configureRequestAndQueryParams(RestClient client, DynamicMap<String> pathParameters, DynamicMap<String> queryParameters) {
        if (pathParameters != null && queryParameters != null) {
            client.setPathParameters(pathParameters);
            client.setQueryParameters(queryParameters);
            doReturn(pathParameters)
                    .when(scriptEngine)
                    .evaluate(eq(pathParameters), any(Message.class), any(FlowContext.class));
            doReturn(queryParameters)
                    .when(scriptEngine)
                    .evaluate(eq(queryParameters), any(Message.class), any(FlowContext.class));
        }
        if (pathParameters != null && queryParameters == null) {
            client.setPathParameters(pathParameters);
            doReturn(pathParameters)
                    .when(scriptEngine)
                    .evaluate(eq(pathParameters), any(Message.class), any(FlowContext.class));
        }
        if (pathParameters == null && queryParameters != null) {
            client.setQueryParameters(queryParameters);
            doReturn(queryParameters)
                    .when(scriptEngine)
                    .evaluate(eq(queryParameters), any(Message.class), any(FlowContext.class));
        }
    }
}
