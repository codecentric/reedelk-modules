package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.commons.MimeToJavaType;
import com.reedelk.runtime.api.message.type.*;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.BiConsumer;

class HttpRequestContentMapper {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestContentMapper.class);

    private HttpRequestContentMapper() {
    }

    /**
     * Given an http request, it finds the most suitable TypedContent for the request.
     * For example, it checks the mime type of the request and it converts it a String
     * if it is a text based mime type, otherwise it keeps as bytes.
     */
    static TypedContent map(HttpRequestWrapper request) {
        // The content stream is fed into a byte sink
        Flux<byte[]> byteArrayStream =
                // we retain and we release it when we read the bytes in the sink below.
                request.data().retain().handle(asByteArrayStream());

        MimeType mimeType = request.mimeType();

        if (String.class == MimeToJavaType.from(mimeType)) {
            return mapStringJavaType(byteArrayStream, mimeType);
        } else {
            // Generic byte array stream.
            Type type = new Type(mimeType, byte[].class);
            return new ByteArrayContent(byteArrayStream, type);
        }
    }

    private static TypedContent mapStringJavaType(Flux<byte[]> byteArrayStream, MimeType mimeType) {
        // If it  is a String, then we check if the charset is present
        // in the mime type to be used for the string conversion.
        Optional<Charset> charset = mimeType.getCharset();

        // Map each byte array of the stream to a string
        Flux<String> streamAsString = byteArrayStream.map(bytes -> {
            Charset conversionCharset = charset.orElseGet(Charset::defaultCharset);
            return new String(bytes, conversionCharset);
        });

        // The TypedContent is String stream.
        return new StringContent(streamAsString, mimeType);
    }

    // TODO: What would happen if an exception is thrown before all the byte buffers have been read!??
    private static BiConsumer<ByteBuf, SynchronousSink<byte[]>> asByteArrayStream() {
        return (byteBuffer, sink) -> {
            try {
                byte[] bytes = new byte[byteBuffer.readableBytes()];
                byteBuffer.readBytes(bytes);
                sink.next(bytes);
            } catch (Exception e) {
                logger.error("Error while feeding input sink", e);
                sink.complete();
            } finally {
                // Each stream byte buffer is reference counted,
                // therefore we must release it after reading its bytes.
                byteBuffer.release();
            }
        };
    }
}
