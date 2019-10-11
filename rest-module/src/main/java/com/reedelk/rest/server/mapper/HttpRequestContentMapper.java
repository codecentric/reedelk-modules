package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.commons.TypedContentFromByteArrayStream;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

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
    static TypedContent<?> map(HttpRequestWrapper request) {
        MimeType mimeType = request.mimeType();
        // The content stream is fed into a byte sink
        // We retain and we release it when we read the bytes in the sink below.
        Flux<byte[]> byteArrayStream = request.data().retain().handle(asByteArrayStream());

        return TypedContentFromByteArrayStream.fromMimeType(byteArrayStream, mimeType);
    }

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
