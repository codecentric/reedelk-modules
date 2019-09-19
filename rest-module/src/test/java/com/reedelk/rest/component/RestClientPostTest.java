package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.POST;
import static com.reedelk.runtime.api.commons.ScriptUtils.EVALUATE_PAYLOAD;
import static com.reedelk.runtime.api.message.type.MimeType.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;

class RestClientPostTest extends RestClientAbstractTest {

    @Nested
    @DisplayName("payload and mime type are correct")
    class PayloadAndContentTypeAreCorrect{

        @Test
        void shouldSetCorrectContentTypeHeaderWhenPayloadIsJson() {
            // Given
            String requestBody = "{\"Name\":\"John\"}";
            String expectedResponseBody = "POST was successful";
            RestClient component = componentWith(POST, baseURL, path, EVALUATE_PAYLOAD);

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(equalToJson(requestBody))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));


            Message payload = MessageBuilder.get().json(requestBody).build();

            // Expect
            AssertThatHttpResponseContent
                    .is(component, payload, flowContext, expectedResponseBody, TEXT);
        }

        @Test
        void shouldSetCorrectContentTypeHeaderWhenPayloadIsText() {
            // Given
            String requestBody = "text payload";
            String expectedResponseBody = "POST was successful";
            RestClient component = componentWith(POST, baseURL, path, EVALUATE_PAYLOAD);

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(equalTo(requestBody))
                    .withHeader(CONTENT_TYPE, equalTo(TEXT.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            Message payload = MessageBuilder.get().text(requestBody).build();

            // Expect
            AssertThatHttpResponseContent
                    .is(component, payload, flowContext, expectedResponseBody, TEXT);
        }

        @Test
        void shouldSetCorrectContentTypeHeaderWhenPayloadIsBinary() {
            // Given
            byte[] requestBody = "My binary request body".getBytes();
            String expectedResponseBody = "POST was successful";
            RestClient component = componentWith(POST, baseURL, path, EVALUATE_PAYLOAD);

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(binaryEqualTo(requestBody))
                    .withHeader(CONTENT_TYPE, equalTo(BINARY.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            Message payload = MessageBuilder.get().binary(requestBody).build();

            // Expect
            AssertThatHttpResponseContent
                    .is(component, payload, flowContext, expectedResponseBody, TEXT);
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
            RestClient component = componentWith(POST, baseURL, path, body);

            Message message = MessageBuilder.get().text("my payload").build();

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(equalTo("hello this is a script"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            mockScriptEvaluation(body, message, "hello this is a script");

            // Expect
            AssertThatHttpResponseContent
                    .is(component, message, flowContext, expectedResponseBody, TEXT);

            verify(newRequestPattern().withoutHeader(CONTENT_TYPE));
        }

        void assertEmptyContentTypeAndPayload(String body, Message message) {
            // Given
            String expectedResponseBody = "It works";
            RestClient component = componentWith(POST, baseURL, path, body);

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(binaryEqualTo(new byte[0]))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            // Expect
            AssertThatHttpResponseContent
                    .is(component, message, flowContext, expectedResponseBody, TEXT);
            verify(newRequestPattern().withoutHeader(CONTENT_TYPE));
        }
    }

    private void mockScriptEvaluation(String inputScript, Message message, Object returnValue) {
        doReturn(returnValue)
                .when(scriptEngine)
                .evaluate(eq(inputScript), eq(message), eq(flowContext));
    }
}
