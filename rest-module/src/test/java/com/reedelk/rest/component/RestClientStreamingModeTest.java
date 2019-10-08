package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.StreamingMode;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.ByteArrayContent;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;

class RestClientStreamingModeTest extends RestClientAbstractTest {

    @DisplayName("Streaming mode NONE tests")
    @Nested
    class StreamingModeNone {

        @ParameterizedTest
        @ValueSource(strings = {"POST", "PUT", "DELETE"})
        void shouldSendBodyWithCorrectContentLengthHeader(String method) {
            // Given
            ClientConfiguration configuration = new ClientConfiguration();
            configuration.setHost(HOST);
            configuration.setPort(PORT);
            configuration.setProtocol(HttpProtocol.HTTP);
            configuration.setBasePath(PATH);
            configuration.setId(UUID.randomUUID().toString());

            RestClient component = clientWith(RestMethod.valueOf(method), BASE_URL, PATH, EVALUATE_PAYLOAD_BODY);
            component.setConfiguration(configuration);
            component.setStreaming(StreamingMode.NONE);

            String requestBody = "{\"Name\":\"John\"}";
            doReturn(Optional.of(requestBody.getBytes()))
                    .when(scriptEngine)
                    .evaluate(eq(EVALUATE_PAYLOAD_BODY), any(Message.class), any(FlowContext.class));

            Mockito.verify(scriptEngine, never())
                    .evaluateStream(any(DynamicValue.class), any(Message.class), any(FlowContext.class));

            givenThat(WireMock.any(urlEqualTo(PATH))
                    .withHeader("Content-Length", equalTo(String.valueOf(requestBody.getBytes().length)))
                    .willReturn(aResponse()
                            .withStatus(200)));


            Message payload = MessageBuilder.get().json(requestBody).build();

            // Expect
            AssertHttpResponse.isSuccessful(component, payload, flowContext);
        }
    }

    @DisplayName("Streaming mode ALWAYS tests")
    @Nested
    class StreamingModeAlways {

        @ParameterizedTest
        @ValueSource(strings = {"POST", "PUT", "DELETE"})
        void shouldSendBodyWithChunkedContent(String method) {
            // Given
            ClientConfiguration configuration = new ClientConfiguration();
            configuration.setHost(HOST);
            configuration.setPort(PORT);
            configuration.setProtocol(HttpProtocol.HTTP);
            configuration.setBasePath(PATH);
            configuration.setId(UUID.randomUUID().toString());

            RestClient component = clientWith(RestMethod.valueOf(method), BASE_URL, PATH, EVALUATE_PAYLOAD_BODY);
            component.setConfiguration(configuration);
            component.setStreaming(StreamingMode.ALWAYS);

            String requestBodyChunk1 = "{\"Name\":";
            String requestBodyChunk2 = "\"John\"}";

            ByteArrayContent byteArrayContent = new ByteArrayContent(Flux.just(requestBodyChunk1.getBytes(), requestBodyChunk2.getBytes()),
                    MimeType.APPLICATION_JSON);

            doReturn(byteArrayContent.stream())
                    .when(scriptEngine)
                    .evaluateStream(eq(EVALUATE_PAYLOAD_BODY), any(Message.class), any(FlowContext.class));

            Mockito.verify(scriptEngine, never())
                    .evaluate(any(DynamicValue.class), any(Message.class), any(FlowContext.class));

            givenThat(WireMock.any(urlEqualTo(PATH))
                    .withHeader("Transfer-Encoding", equalTo("chunked"))
                    .willReturn(aResponse()
                            .withStatus(200)));


            Message payload = MessageBuilder.get().typedContent(byteArrayContent).build();

            // Expect
            AssertHttpResponse.isSuccessful(component, payload, flowContext);
        }
    }

    @DisplayName("Streaming mode AUTO tests")
    @Nested
    class StreamingModeAuto {

        @ParameterizedTest
        @ValueSource(strings = {"POST", "PUT", "DELETE"})
        void shouldSendBodyWithChunkedContent(String method) {
            // Given
            ClientConfiguration configuration = new ClientConfiguration();
            configuration.setHost(HOST);
            configuration.setPort(PORT);
            configuration.setProtocol(HttpProtocol.HTTP);
            configuration.setBasePath(PATH);
            configuration.setId(UUID.randomUUID().toString());

            RestClient component = clientWith(RestMethod.valueOf(method), BASE_URL, PATH, EVALUATE_PAYLOAD_BODY);
            component.setConfiguration(configuration);
            component.setStreaming(StreamingMode.AUTO);

            String requestBodyChunk1 = "{\"Name\":";
            String requestBodyChunk2 = "\"John\"}";

            ByteArrayContent byteArrayContent = new ByteArrayContent(Flux.just(requestBodyChunk1.getBytes(), requestBodyChunk2.getBytes()),
                    MimeType.APPLICATION_JSON);

            doReturn(byteArrayContent.stream())
                    .when(scriptEngine)
                    .evaluateStream(eq(EVALUATE_PAYLOAD_BODY), any(Message.class), any(FlowContext.class));

            Mockito.verify(scriptEngine, never())
                    .evaluate(any(DynamicValue.class), any(Message.class), any(FlowContext.class));

            givenThat(WireMock.any(urlEqualTo(PATH))
                    .withHeader("Transfer-Encoding", equalTo("chunked"))
                    .willReturn(aResponse()
                            .withStatus(200)));


            Message payload = MessageBuilder.get().typedContent(byteArrayContent).build();

            // Expect
            AssertHttpResponse.isSuccessful(component, payload, flowContext);
        }

        @ParameterizedTest
        @ValueSource(strings = {"POST", "PUT", "DELETE"})
        void shouldSendBodyWithContentLengthHeader(String method) {
            // Given
            ClientConfiguration configuration = new ClientConfiguration();
            configuration.setHost(HOST);
            configuration.setPort(PORT);
            configuration.setProtocol(HttpProtocol.HTTP);
            configuration.setBasePath(PATH);
            configuration.setId(UUID.randomUUID().toString());

            RestClient component = clientWith(RestMethod.valueOf(method), BASE_URL, PATH, EVALUATE_PAYLOAD_BODY);
            component.setConfiguration(configuration);
            component.setStreaming(StreamingMode.AUTO);

            String requestBody = "{\"Name\":\"John\"}";

            ByteArrayContent byteArrayContent = new ByteArrayContent(requestBody.getBytes(), MimeType.APPLICATION_JSON);

            doReturn(Optional.of(requestBody.getBytes()))
                    .when(scriptEngine)
                    .evaluate(eq(EVALUATE_PAYLOAD_BODY), any(Message.class), any(FlowContext.class));

            Mockito.verify(scriptEngine, never())
                    .evaluateStream(any(DynamicValue.class), any(Message.class), any(FlowContext.class));

            givenThat(WireMock.any(urlEqualTo(PATH))
                    .withHeader("Content-Length", equalTo(String.valueOf(requestBody.getBytes().length)))
                    .willReturn(aResponse()
                            .withStatus(200)));


            Message payload = MessageBuilder.get().typedContent(byteArrayContent).build();

            // Expect
            AssertHttpResponse.isSuccessful(component, payload, flowContext);
        }
    }
}
