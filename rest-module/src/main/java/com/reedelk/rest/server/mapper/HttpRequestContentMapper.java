package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.commons.JavaType;
import com.reedelk.runtime.api.message.type.*;
import io.netty.buffer.ByteBuf;
import io.netty.util.IllegalReferenceCountException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.BiConsumer;

class HttpRequestContentMapper {

    /**
     * Given an http request, it finds the most suitable TypedContent for the request.
     * For example, it checks the mime type of the request and it converts it a String
     * if it is a text based mime type, otherwise it keeps as bytes.
     */
    static TypedContent map(HttpRequestWrapper request) {
        // Map the request content and forward it to
        // a sink which maps it to a byte buffer.
        Flux<byte[]> byteArrayStream = request
                .data()
                .retain()
                .handle(asByteArrayStream());

        MimeType mimeType = request.mimeType();

        Class<?> javaType = JavaType.from(mimeType);
        if (javaType == String.class) {
            // If it  is a String, then we check the charset if present
            // in the mime type to be used for the string conversion.
            Optional<Charset> charset = mimeType.getCharset();

            // Map each byte array of the stream to a string
            Flux<String> streamAsString = byteArrayStream.map(bytes -> {
                Charset conversionCharset = charset.orElseGet(Charset::defaultCharset);
                return new String(bytes, conversionCharset);
            });

            // The TypedContent is String stream.
            return new StringStreamContent(streamAsString, mimeType);

        } else {
            // Generic byte array stream.
            Type type = new Type(mimeType, byte[].class);
            return new ByteArrayStreamContent(byteArrayStream, type);
        }
    }

    private static BiConsumer<ByteBuf, SynchronousSink<byte[]>> asByteArrayStream() {
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
