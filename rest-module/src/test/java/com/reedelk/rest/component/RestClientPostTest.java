package com.reedelk.rest.component;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.POST;
import static com.reedelk.runtime.api.message.content.MimeType.*;
import static org.mockito.ArgumentMatchers.any;
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
            RestClient component = clientWith(POST, BASE_URL, PATH, EVALUATE_PAYLOAD_BODY);

            doReturn(Optional.of(requestBody.getBytes()))
                    .when(scriptEngine)
                    .evaluate(eq(EVALUATE_PAYLOAD_BODY), any(FlowContext.class), any(Message.class));

            givenThat(post(urlEqualTo(PATH))
                    .withRequestBody(equalToJson(requestBody))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));


            Message payload = MessageBuilder.get().json(requestBody).build();

            // Expect
            AssertHttpResponse
                    .isSuccessful(component, payload, flowContext, expectedResponseBody, TEXT);
        }

        @Test
        void shouldSetCorrectContentTypeHeaderWhenPayloadIsText() {
            // Given
            String requestBody = "text payload";
            String expectedResponseBody = "POST was successful";
            RestClient component = clientWith(POST, BASE_URL, PATH, EVALUATE_PAYLOAD_BODY);

            doReturn(Optional.of(requestBody.getBytes()))
                    .when(scriptEngine)
                    .evaluate(eq(EVALUATE_PAYLOAD_BODY), any(FlowContext.class), any(Message.class));

            givenThat(post(urlEqualTo(PATH))
                    .withRequestBody(equalTo(requestBody))
                    .withHeader(CONTENT_TYPE, equalTo(TEXT.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            Message payload = MessageBuilder.get().text(requestBody).build();

            // Expect
            AssertHttpResponse
                    .isSuccessful(component, payload, flowContext, expectedResponseBody, TEXT);
        }

        @Test
        void shouldSetCorrectContentTypeHeaderWhenPayloadIsBinary() {
            // Given
            byte[] requestBody = "My binary request body".getBytes();
            String expectedResponseBody = "POST was successful";
            RestClient component = clientWith(POST, BASE_URL, PATH, EVALUATE_PAYLOAD_BODY);

            doReturn(Optional.of(requestBody))
                    .when(scriptEngine)
                    .evaluate(eq(EVALUATE_PAYLOAD_BODY), any(FlowContext.class), any(Message.class));

            givenThat(post(urlEqualTo(PATH))
                    .withRequestBody(binaryEqualTo(requestBody))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_BINARY.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            Message payload = MessageBuilder.get().binary(requestBody).build();

            // Expect
            AssertHttpResponse
                    .isSuccessful(component, payload, flowContext, expectedResponseBody, TEXT);
        }

        @Test
        void shouldNotSetContentTypeHeaderWhenPayloadIsEmpty() {
            // Given
            Message emptyPayload = MessageBuilder.get().build();

            // Expect
            assertEmptyContentTypeAndPayload(EVALUATE_PAYLOAD_BODY, emptyPayload);
        }

        @Test
        void shouldNotSetContentTypeHeaderAndSendEmptyPayloadWhenBodyIsNull() {
            // Given
            DynamicByteArray body = null;
            Message emptyPayload = MessageBuilder.get().build();

            // Expect
            assertEmptyContentTypeAndPayload(body, emptyPayload);
        }

        @Test
        void shouldNotSetContentTypeHeaderAndSendEmptyPayloadWhenBodyIsEmptyString() {
            // Given
            DynamicByteArray body = DynamicByteArray.from(" ", testModuleId);
            Message emptyPayload = MessageBuilder.get().build();

            // Expect
            assertEmptyContentTypeAndPayload(body, emptyPayload);
        }

        @Test
        void shouldNotSetContentTypeHeaderAndSendEmptyPayloadWhenBodyIsEmptyScript() {
            // Given
            DynamicByteArray body = DynamicByteArray.from("#[]", testModuleId);
            Message emptyPayload = MessageBuilder.get().build();

            // Expect
            assertEmptyContentTypeAndPayload(body, emptyPayload);
        }

        @Test
        void shouldNotSetContentTypeHeaderWhenPayloadIsScript() {
            // Given
            DynamicByteArray body = DynamicByteArray.from("#['hello this is a script']", testModuleId);
            String expectedResponseBody = "POST was successful";
            RestClient component = clientWith(POST, BASE_URL, PATH, body);

            doReturn(Optional.of("hello this is a script".getBytes()))
                    .when(scriptEngine)
                    .evaluate(eq(body), any(FlowContext.class), any(Message.class));

            Message message = MessageBuilder.get().text("my payload").build();

            givenThat(post(urlEqualTo(PATH))
                    .withRequestBody(equalTo("hello this is a script"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            // Expect
            AssertHttpResponse
                    .isSuccessful(component, message, flowContext, expectedResponseBody, TEXT);

            verify(newRequestPattern().withoutHeader(CONTENT_TYPE));
        }

        void assertEmptyContentTypeAndPayload(DynamicByteArray body, Message message) {
            // Given
            String expectedResponseBody = "It works";
            RestClient component = clientWith(POST, BASE_URL, PATH, body);

            givenThat(post(urlEqualTo(PATH))
                    .withRequestBody(binaryEqualTo(new byte[0]))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            // Expect
            AssertHttpResponse
                    .isSuccessful(component, message, flowContext, expectedResponseBody, TEXT);
            verify(newRequestPattern().withoutHeader(CONTENT_TYPE));
        }
    }

    @Test
    void shouldPostThrowExceptionWhenResponseNot2xx() {
        // Given
        String expectedErrorMessage = "Error exception caused by XYZ";
        RestClient component = clientWith(POST, BASE_URL, PATH);

        givenThat(post(urlEqualTo(PATH))
                .willReturn(aResponse()
                        .withStatus(507)
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody(expectedErrorMessage)));

        Message emptyPayload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse
                .isNotSuccessful(component, emptyPayload, flowContext, expectedErrorMessage);
    }
}
