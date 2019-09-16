package com.reedelk.rest.component;

import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.POST;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestClientCommonTests extends RestClientAbstractTest {

    private RestClient component = componentWith(baseURL, path, POST);

    @Nested
    @DisplayName("not successful response")
    class NotSuccessfulResponse {

        @Test
        void shouldPostThrowExceptionWhenResponseNot2xx() {
            // Given
            mockServer.stubFor(post(urlEqualTo(path))
                    .withRequestBody(binaryEqualTo(new byte[0]))
                    .willReturn(aResponse()
                            .withHeader(CONTENT_TYPE, TEXT.toString())
                            .withStatus(500)
                            .withBody("Error exception caused by XYZ")));

            Message emptyPayload = MessageBuilder.get().build();

            // Expect
            ESBException thrown = assertThrows(ESBException.class,
                    () -> component.apply(emptyPayload, flowContext));

            assertThat(thrown).hasMessage("Error exception caused by XYZ");
        }
    }

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
            assertExpectedPath(path, expectedPath, pathParameters);
        }

        @Test
        void shouldCorrectlyBuildRequestUriWithQueryParams() {
            // Given
            String path = "/resource";
            String expectedPath = "/resource?query1=value1&query2=value2";

            Map<String,String> queryParameters = ImmutableMap.of("query1", "value1", "query2", "value2");

            // Expect
            assertExpectedPath(path, expectedPath, ImmutableMap.of(), queryParameters);
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

    void assertExpectedPath(String path, String expectedPath, Map<String,String> pathParameters) {
        assertExpectedPath(path, expectedPath, pathParameters,null);
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
        RestClient component = componentWith(baseURL, path, POST);
        component.setPathParameters(pathParameters);
        component.setQueryParameters(queryParameters);
        Message outMessage = component.apply(message, flowContext);

        // Then
        assertContent(outMessage, expectedResponseBody, TEXT);
    }
}
