package com.reedelk.rest.server;

import com.reedelk.rest.commons.HttpHeader;
import com.reedelk.rest.configuration.RestListenerErrorResponse;
import com.reedelk.rest.configuration.RestListenerResponse;
import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Map;
import java.util.Optional;

public class MessageToHttpResponse {


    public static Publisher<byte[]> from(Throwable exception,
                                    Context context,
                                    HttpServerResponse response,
                                    RestListenerErrorResponse listenerErrorResponse) {

        // Handle status

        // Handle content type

        // Handle additional headers

        return Mono.just(exception.getMessage().getBytes());

    }


    public static Publisher<byte[]> from(Message message,
                                 Context context,
                                 HttpServerResponse response,
                                 RestListenerResponse listenerResponse) {
        // Handle status
        if (listenerResponse.isUseStatus()) {
            if (listenerResponse.isUseReasonPhrase()) {
                String customReasonPhrase = listenerResponse.getReasonPhrase();
            }
            response.status(HttpResponseStatus.valueOf(listenerResponse.getStatus()));
        } else {
            response.status(HttpResponseStatus.OK);
        }

        Publisher<byte[]> data = Mono.empty();

        if (listenerResponse.isUseBody()) {
            // Custom body
            // Evaluate with script... (the body could be a variable in the context)...
        } else {
            // The content type comes from the message typed content
            TypedContent<?> typedContent = message.getTypedContent();
            Type type = typedContent.type();
            MimeType contentType = type.getMimeType();
            response.addHeader(HttpHeader.CONTENT_TYPE, contentType.toString());
            data = typedContent.asByteArrayStream();
        }

        // Handle content type
        Map<String, String> additionalHeaders = listenerResponse.getHeaders();
        // 1. If the content type is present as additional header, then we use that one
        if (additionalHeaders.containsKey(HttpHeader.CONTENT_TYPE)) {// case insensitive
            // Content type from additional headers
        };


        return data;
    }

    private static Optional<String> getMatchingHeader(HttpHeaders headers, String targetHeaderName) {
        for (String headerName : headers.names()) {
            if (headerName.toLowerCase().equals(targetHeaderName.toLowerCase())) {
                return Optional.of(headerName);
            }
        }
        return Optional.empty();
    }
}
