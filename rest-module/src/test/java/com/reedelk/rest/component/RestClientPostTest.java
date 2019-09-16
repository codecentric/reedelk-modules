package com.reedelk.rest.component;

import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.POST;
import static com.reedelk.runtime.api.commons.ScriptUtils.EVALUATE_PAYLOAD;
import static com.reedelk.runtime.api.message.type.MimeType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class RestClientPostTest extends RestClientAbstractTest {

    @Mock
    private ScriptEngineService scriptEngine;

    private RestClient component = componentWith(baseURL, path, POST);

    @BeforeEach
    void setUp() {
        super.setUp();
        setScriptEngine(component, scriptEngine);
    }

    @Nested
    @DisplayName("payload and mime type are correct")
    class PayloadAndContentTypeAreCorrect{

        @Test
        void shouldSetCorrectContentTypeHeaderWhenPayloadIsJson() {
            // Given
            String requestBody = "{\"Name\":\"John\"}";
            String expectedResponseBody = "POST was successful";

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(equalToJson(requestBody))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));


            Message payload = MessageBuilder.get().json(requestBody).build();

            // When
            component.setBody(EVALUATE_PAYLOAD);
            Message outMessage = component.apply(payload, flowContext);

            // Then
            assertContent(outMessage, expectedResponseBody, TEXT);
        }

        @Test
        void shouldSetCorrectContentTypeHeaderWhenPayloadIsText() {
            // Given
            String requestBody = "text payload";
            String expectedResponseBody = "POST was successful";

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(equalTo(requestBody))
                    .withHeader(CONTENT_TYPE, equalTo(TEXT.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            Message payload = MessageBuilder.get().text(requestBody).build();

            // When
            component.setBody(EVALUATE_PAYLOAD);
            Message outMessage = component.apply(payload, flowContext);

            // Then
            assertContent(outMessage, expectedResponseBody, TEXT);
        }

        @Test
        void shouldSetCorrectContentTypeHeaderWhenPayloadIsBinary() {
            // Given
            byte[] requestBody = "My binary request body".getBytes();
            String expectedResponseBody = "POST was successful";

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(binaryEqualTo(requestBody))
                    .withHeader(CONTENT_TYPE, equalTo(BINARY.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            Message payload = MessageBuilder.get().binary(requestBody).build();

            // When
            component.setBody(EVALUATE_PAYLOAD);
            Message outMessage = component.apply(payload, flowContext);

            // Then
            assertContent(outMessage, expectedResponseBody, TEXT);
        }

        @Test
        void shouldNotSetContentTypeHeaderWhenPayloadIsEmpty() {
            // Given
            String body = EVALUATE_PAYLOAD;
            Message emptyPayload = MessageBuilder.get().build();

            // Expect
            assertEmptyContentTypeAndPayload(body, emptyPayload);
        }

        @Test
        void shouldNotSetContentTypeHeaderAndSendEmptyPayloadWhenBodyIsNull() {
            // Given
            String body = null;
            Message emptyPayload = MessageBuilder.get().build();

            // Expect
            assertEmptyContentTypeAndPayload(body, emptyPayload);
        }

        @Test
        void shouldNotSetContentTypeHeaderAndSendEmptyPayloadWhenBodyIsEmptyString() {
            // Given
            String body = " ";
            Message emptyPayload = MessageBuilder.get().build();

            // Expect
            assertEmptyContentTypeAndPayload(body, emptyPayload);
        }

        @Test
        void shouldNotSetContentTypeHeaderAndSendEmptyPayloadWhenBodyIsEmptyScript() {
            // Given
            String body = "#[]";
            Message emptyPayload = MessageBuilder.get().build();

            // Expect
            assertEmptyContentTypeAndPayload(body, emptyPayload);
        }

        @Test
        void shouldNotSetContentTypeHeaderWhenPayloadIsScript() {
            // Given
            String body = "#['hello this is a script']";
            String expectedResponseBody = "POST was successful";

            Message message = MessageBuilder.get().build();

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(equalTo("hello this is a script"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            mockScriptEvaluation(body, message, "hello this is a script");

            // When
            component.setBody(body);
            Message outMessage = component.apply(message, flowContext);

            // Then
            assertContent(outMessage, expectedResponseBody);
            verify(newRequestPattern().withoutHeader(CONTENT_TYPE));
        }

        void assertEmptyContentTypeAndPayload(String body, Message message) {
            // Given
            String expectedResponseBody = "It works";
            givenThat(post(urlEqualTo(path))
                    .withRequestBody(binaryEqualTo(new byte[0]))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            // When
            component.setBody(body);
            Message outMessage = component.apply(message, flowContext);

            // Then
            assertContent(outMessage, expectedResponseBody, TEXT);
            verify(newRequestPattern().withoutHeader(CONTENT_TYPE));
        }
    }

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

    private void mockScriptEvaluation(String inputScript, Message message, Object returnValue) {
        doReturn(returnValue)
                .when(scriptEngine)
                .evaluate(eq(inputScript), eq(message));
    }

    void assertExpectedPath(String path, String expectedPath, Map<String,String> pathParameters) {
        assertExpectedPath(path, expectedPath, pathParameters,null);
    }

    void assertExpectedPath(String path, String expectedPath, Map<String,String> pathParameters, Map<String,String> queryParameters) {
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
