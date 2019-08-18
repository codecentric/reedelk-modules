package com.reedelk.rest.server;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.ByteArrayStreamType;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.TypedContent;
import io.netty.buffer.ByteBuf;
import io.netty.util.IllegalReferenceCountException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

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

        // Map the request content and forward it to
        // a sink which maps it to a byte buffer.
        Flux<byte[]> map = request.receive().retain().handle(byteBuffSink());

        MimeType mimeType = request.mimeType();

        TypedContent content = new ByteArrayStreamType(map, mimeType);

        messageBuilder.typedContent(content);

        return messageBuilder.build();
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
