package com.reedelk.rest.server;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.*;
import io.netty.buffer.ByteBuf;
import io.netty.util.IllegalReferenceCountException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.reedelk.rest.commons.InboundProperty.*;

class HttpRequestToMessage {

    static Message from(HttpRequestWrapper request) {

        MessageBuilder messageBuilder =
                MessageBuilder.get()
                        .addInboundProperty(path(), request.uri())
                        .addInboundProperty(method(), request.method())
                        .addInboundProperty(headers(), request.headers())
                        .addInboundProperty(pathParams(), request.params())
                        .addInboundProperty(queryParams(), request.queryParams());

        TypedContent content = getTypedContent(request);

        messageBuilder.typedContent(content);

        return messageBuilder.build();
    }

    /**
     * Given an http request, it find the most suitable TypedContent for the request.
     * For example, it checks the mime type of the request and it converts it a String
     * if it is a text based mime type, otherwise it keeps as bytes.
     */
    private static TypedContent getTypedContent(HttpRequestWrapper request) {
        // Map the request content and forward it to
        // a sink which maps it to a byte buffer.
        Flux<byte[]> byteArrayStream = request
                .receive()
                .retain()
                .handle(byteBuffSink());

        MimeType mimeType = request.mimeType();
        Type type = new Type(mimeType);

        if (type.getTypeClass() == String.class) {
            // If it  is a String, then we check the charset if present
            // in the mime type to be used for the string conversion.
            Optional<Charset> charset = mimeType.getCharset();
            Flux<String> stringStream = byteArrayStream.map(bytes -> {
                Charset conversionCharset = charset.orElseGet(Charset::defaultCharset);
                return new String(bytes, conversionCharset);
            });
            return new StringStreamContent(stringStream, mimeType);
        }

        return new ByteArrayStreamContent(byteArrayStream, mimeType);
    }

    private static BiConsumer<ByteBuf, SynchronousSink<byte[]>> byteBuffSink() {
        return (byteBuffer, sink) -> {
            try {
                byte[] bytes = new byte[byteBuffer.readableBytes()];
                byteBuffer.readBytes(bytes);
                sink.next(bytes);
                byteBuffer.release();
            } catch (IllegalReferenceCountException e) {
                sink.complete();
            }
        };
    }
}
