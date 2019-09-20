package com.reedelk.rest.component;

import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.NMapEvaluation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.POST;
import static com.reedelk.rest.utils.TestTag.INTEGRATION;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@Tag(INTEGRATION)
class RestClientCommonTests extends RestClientAbstractTest {

    @Nested
    @DisplayName("request uri is correct")
    class RequestUri {

        @Test
        void shouldCorrectlyBuildRequestUriWithPathParams() {
            // Given
            String path = "/resource/{id}/group/{group}";
            String expectedPath = "/resource/aabbccddeeff/group/user";

            Map<String, String> pathParameters = ImmutableMap.of("id", "aabbccddeeff", "group", "user");

            // Expect
            assertExpectedPath(path, expectedPath, pathParameters, null);
        }

        @Test
        void shouldCorrectlyBuildRequestUriWithQueryParams() {
            // Given
            String path = "/resource";
            String expectedPath = "/resource?query1=value1&query2=value2";

            Map<String,String> queryParameters = ImmutableMap.of("query1", "value1", "query2", "value2");

            // Expect
            assertExpectedPath(path, expectedPath, null, queryParameters);
        }

        @Test
        void shouldCorrectlyBuildRequestUriWithPathAndQueryParams() {
            // Given
            String path = "/resource/{id}/title/{title}";
            String expectedPath = "/resource/aabb1122/title/manager?query1=value1&query2=value2";

            Map<String,String> queryParameters = ImmutableMap.of("query1", "value1", "query2", "value2");
            Map<String,String> pathParameters = ImmutableMap.of("id", "aabb1122", "title", "manager");

            // Expect
            assertExpectedPath(path, expectedPath, pathParameters, queryParameters);
        }
    }

    void assertExpectedPath(String path, String expectedPath, Map<String,String> pathParameters, Map<String,String> queryParameters) {
        // Given
        String expectedResponseBody = "It works";
        givenThat(post(urlEqualTo(expectedPath))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(expectedResponseBody)
                        .withHeader(CONTENT_TYPE, TEXT.toString())));

        Message message = MessageBuilder.get().empty().build();

        // When
        RestClient component = componentWith(POST, baseURL, path);
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

        // Expect
        AssertThatHttpResponseContent
                .isSuccessful(component, message, flowContext, expectedResponseBody, TEXT);
    }
}
