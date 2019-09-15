package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.POST;
import static com.reedelk.runtime.api.commons.ScriptUtils.EVALUATE_PAYLOAD;
import static com.reedelk.runtime.api.message.type.MimeType.APPLICATION_JSON;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestClientPostTest extends RestClientAbstractTest {

    private RestClient component = componentWith(baseURL, path, POST);

    @Nested
    @DisplayName("payload mime type assigned correctly")
    class PayloadMimeTypeAssignedCorrectly {

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
            assertContentIs(outMessage, expectedResponseBody, TEXT);
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
            assertContentIs(outMessage, expectedResponseBody, TEXT);
        }

        @Test
        void shouldNotSetContentTypeHeaderWhenPayloadIsEmpty() {
            // Given
            String expectedResponseBody = "It works";

            givenThat(post(urlEqualTo(path))
                    .withRequestBody(binaryEqualTo(new byte[0]))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(expectedResponseBody)
                            .withHeader(CONTENT_TYPE, TEXT.toString())));

            Message emptyPayload = MessageBuilder.get().build();

            // When
            component.setBody(EVALUATE_PAYLOAD);
            Message outMessage = component.apply(emptyPayload, flowContext);

            // Then
            assertContentIs(outMessage, expectedResponseBody, TEXT);

            verify(RequestPatternBuilder.newRequestPattern().withoutHeader("Content-Type"));
        }
    }

    @Nested
    @DisplayName("not successful response")
    class UnsuccessfulResponse {

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
}
