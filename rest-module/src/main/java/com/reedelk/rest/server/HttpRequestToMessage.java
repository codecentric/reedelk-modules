package com.reedelk.rest.server;

import com.reedelk.rest.commons.AsSerializableMap;
import com.reedelk.rest.commons.HttpHeadersAsMap;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.ByteArrayStreamType;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.TypedContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.IllegalReferenceCountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;

import java.util.List;
import java.util.Map;

import static com.reedelk.rest.commons.InboundProperty.*;

class HttpRequestToMessage {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestToMessage.class);

    static Message from(HttpServerRequest request) {

        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        Map<String, List<String>> queryParameters = decoder.parameters();

        MessageBuilder messageBuilder = MessageBuilder.get()
                .addInboundProperty(path(), request.uri())
                .addInboundProperty(method(), request.method().name())
                .addInboundProperty(headers(), HttpHeadersAsMap.of(request.requestHeaders()))
                .addInboundProperty(pathParams(), AsSerializableMap.of(request.params()))
                .addInboundProperty(queryParams(), AsSerializableMap.of(queryParameters));

        MimeType mimeType = mimeTypeOf(request);


        Flux<byte[]> map = request.receive().retain().handle((byteBuffer, sink) -> {
            try {
                byte[] bytes = new byte[byteBuffer.readableBytes()];
                byteBuffer.readBytes(bytes);
                sink.next(bytes);
                byteBuffer.release();
            } catch (IllegalReferenceCountException e) {
                sink.complete();
            }
        });


        TypedContent content = new ByteArrayStreamType(map, mimeType);

        messageBuilder.typedContent(content);

        return messageBuilder.build();
    }

    private static MimeType mimeTypeOf(HttpServerRequest request) {
        if (request.requestHeaders().contains(HttpHeaderNames.CONTENT_TYPE)) {
            String contentType = request.requestHeaders().get(HttpHeaderNames.CONTENT_TYPE);
            try {
                return MimeType.parse(contentType);
            } catch (Exception e) {
                logger.warn(String.format("Could not parse content type '%s'", contentType), e);
            }
        }
        return MimeType.UNKNOWN;
    }
}
