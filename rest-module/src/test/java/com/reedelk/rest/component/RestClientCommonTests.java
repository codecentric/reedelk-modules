package com.reedelk.rest.component;

import org.junit.jupiter.api.Tag;

import static com.reedelk.rest.commons.RestMethod.POST;
import static com.reedelk.rest.utils.TestTag.INTEGRATION;

@Tag(INTEGRATION)
class RestClientCommonTests extends RestClientAbstractTest {

    private RestClient component = componentWith(POST, baseURL, path);

    /**
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
    }*/
}
