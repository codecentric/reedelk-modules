package com.reedelk.rest.client;

import com.reedelk.rest.commons.ContentType;
import com.reedelk.rest.commons.HttpHeader;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.util.Optional;

public class MessageBodyProvider {

    public static BodyProvider from(Message message, String body, ScriptEngineService scriptEngine) {
        // This code is only executed if and only if the request is
        // either POST,PUT or DELETE. For all other HTTP methods this is not executed.
        return (request) -> {
            if (StringUtils.isBlank(body)) {
                // No content type header
                return new EmptyBodyProvider();
            } else if (ScriptUtils.isScript(body)) {
                return fromScript(message, body, scriptEngine, request);
            } else {
                return fromText(body);
            }
        };
    }

    private static BodyDataProvider fromScript(Message message, String body, ScriptEngineService scriptEngine, HttpClientRequest request) {
        if (ScriptUtils.isMessagePayload(body)) {

            ContentType.from(message).ifPresent(contentType -> request.addHeader(HttpHeader.CONTENT_TYPE, contentType));

            if (message.getContent().isStream()) {
                // The payload is a stream based payload.
                // We don't know the content length, since it is not loaded into memory.
                // In this case the transfer encoding will be chunked (the length is not set).
                Publisher<byte[]> byteArrayStream = message.getContent().asByteArrayStream();
                return new ByteArrayStreamBodyProvider(byteArrayStream);
            } else {
                // The payload is not a stream based payload.
                // We know the content length, since it is completely loaded into memory.
                byte[] bodyAsBytes = message.getContent().asByteArray();
                Mono<byte[]> dataStream = Mono.just(bodyAsBytes);
                return new ByteArrayBodyProvider(dataStream, bodyAsBytes.length);
            }
        } else if (ScriptUtils.isEmpty(body)) {
            // If the script is empty, there is nothing to evaluate.
            return new EmptyBodyProvider();

        } else {
            // The is a script: we evaluate it and set it the result.
            Object result = scriptEngine.evaluate(body, message);
            byte[] bodyAsBytes = result.toString().getBytes();
            Mono<byte[]> dataStream = Mono.just(bodyAsBytes);
            return new ByteArrayBodyProvider(dataStream, bodyAsBytes.length);
        }
    }

    private static BodyDataProvider fromText(String body) {
        // The body is not a script, it is just plain text.
        // We know the number of bytes to be sent.
        // Transfer encoding will NOT be chunked.
        byte[] bodyAsBytes = body.getBytes();
        Mono<byte[]> dataStream = Mono.just(bodyAsBytes);
        return new ByteArrayBodyProvider(dataStream, bodyAsBytes.length);
    }

    static class ByteArrayStreamBodyProvider implements BodyDataProvider {

        private final Publisher<byte[]> data;

        ByteArrayStreamBodyProvider(Publisher<byte[]> data) {
            this.data = data;
        }

        @Override
        public Publisher<? extends ByteBuf> get() {
            return Flux.from(data).map(Unpooled::wrappedBuffer);
        }

        @Override
        public Optional<Integer> length() {
            return Optional.empty();
        }
    }

    static class ByteArrayBodyProvider implements BodyDataProvider {

        private final Publisher<byte[]> data;
        private final int length;

        ByteArrayBodyProvider(Publisher<byte[]> data, int length) {
            this.data = data;
            this.length = length;
        }

        @Override
        public Publisher<? extends ByteBuf> get() {
            return Flux.from(data).map(Unpooled::wrappedBuffer);
        }

        @Override
        public Optional<Integer> length() {
            return Optional.of(length);
        }
    }

    static class EmptyBodyProvider implements BodyDataProvider {
        @Override
        public Publisher<? extends ByteBuf> get() {
            return Mono.empty();
        }

        @Override
        public Optional<Integer> length() {
            return Optional.empty();
        }
    }
}
