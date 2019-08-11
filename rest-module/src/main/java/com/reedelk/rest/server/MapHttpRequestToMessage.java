package com.reedelk.rest.server;

import com.reedelk.rest.commons.AsSerializableMap;
import com.reedelk.rest.commons.HttpHeadersAsMap;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.reedelk.rest.commons.InboundProperty.*;

class MapHttpRequestToMessage {

    private static final Logger logger = LoggerFactory.getLogger(MapHttpRequestToMessage.class);

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

        // We set the mime type in the message
        messageBuilder.mimeType(mimeType);

        // We convert the flux to the correct type given the mime type
        Class<?> javaClazz = Type.JavaFromMimeType.of(mimeType);
        if (javaClazz == String.class) {
            Charset charset = mimeType.getCharset().orElse(Charset.defaultCharset());
            Flux<String> stringFlux = request.receive()
                    .map(byteBuf -> byteBuf.toString(charset));
            messageBuilder.content(stringFlux);
        } else {
            // In any other case this is just binary data
            Flux<byte[]> map = request.receive().map(ByteBuf::array);
            messageBuilder.content(map);
        }

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
