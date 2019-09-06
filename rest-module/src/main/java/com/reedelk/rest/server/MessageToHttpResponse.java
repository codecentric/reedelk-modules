package com.reedelk.rest.server;

import com.reedelk.rest.commons.IsBoolean;
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

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class MessageToHttpResponse {


    public static Publisher<byte[]> from(Throwable exception,
                                         Context context,
                                         HttpServerResponse response,
                                         RestListenerErrorResponse errorResponseConfig) {
        statusFrom(INTERNAL_SERVER_ERROR,
                errorResponseConfig.getUseStatus(),
                errorResponseConfig.getStatus())
                .ifPresent(response::status);

        addAdditionalHeaders(
                response.responseHeaders(),
                errorResponseConfig.getHeaders());

        return Mono.just(exception.getMessage().getBytes());

    }


    public static Publisher<byte[]> from(Message message,
                                         Context context,
                                         HttpServerResponse response,
                                         RestListenerResponse responseConfig) {
        statusFrom(OK,
                responseConfig.getUseStatus(),
                responseConfig.getStatus())
                .ifPresent(response::status);

        contentTypeFrom(
                message,
                responseConfig)
                .ifPresent(contentType -> response.addHeader(CONTENT_TYPE, contentType));

        addAdditionalHeaders(
                response.responseHeaders(),
                responseConfig.getHeaders());

        if (IsBoolean._true(responseConfig.getUseBody())) {
            // Custom body - evaluate script - or just return the value (if it is not a script)
            // TODO: Complete me.
            return Mono.empty();

        } else {
            // The content type comes from the message typed content
            TypedContent<?> typedContent = message.getTypedContent();
            return typedContent.asByteArrayStream();
        }
    }

    /**
     * For each additional header, if present in the current headers it gets replaced,
     * otherwise it is  added to the current headers collection.
     *
     * @param currentHeaders    the current headers.
     * @param additionalHeaders additional user defined headers.
     */
    private static void addAdditionalHeaders(HttpHeaders currentHeaders, Map<String, String> additionalHeaders) {
        additionalHeaders.forEach((headerName, headerValue) -> {
            Optional<String> optionalMatchingHeaderName = matchingHeader(currentHeaders, headerName);
            if (optionalMatchingHeaderName.isPresent()) {
                String matchingHeaderName = optionalMatchingHeaderName.get();
                currentHeaders.remove(matchingHeaderName);
                currentHeaders.add(headerName.toLowerCase(), headerValue);
            } else {
                currentHeaders.add(headerName.toLowerCase(), headerValue);
            }
        });
    }

    private static Optional<String> contentTypeFrom(Message message, RestListenerResponse responseConfig) {
        // If the content type is a custom body, the developer MUST define the content type in the response config.
        if (IsBoolean._true(responseConfig.getUseBody())) {
            // Then we use the content type from the payload's mime type.
            TypedContent<?> typedContent = message.getTypedContent();
            Type type = typedContent.type();
            MimeType contentType = type.getMimeType();
            return Optional.of(contentType.toString());
        }
        return Optional.empty();
    }

    private static Optional<HttpResponseStatus> statusFrom(HttpResponseStatus defaultStatus, Boolean useStatus, Integer statusCode) {
        int code = defaultStatus.code();
        if (IsBoolean._true(useStatus)) {
            code = statusCode;
        }
        return Optional.of(valueOf(code));
    }

    // Returns the matching header name
    private static Optional<String> matchingHeader(HttpHeaders headers, String targetHeaderName) {
        for (String headerName : headers.names()) {
            if (headerName.toLowerCase().equals(targetHeaderName.toLowerCase())) {
                return Optional.of(headerName);
            }
        }
        return Optional.empty();
    }
}
