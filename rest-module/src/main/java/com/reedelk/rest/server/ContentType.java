package com.reedelk.rest.server;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;

import java.util.Optional;

public class ContentType {

    public static Optional<String> from(String responseBody, Message message) {
        // If the content type is a custom body, the developer MUST define the content type in the response config.
        if (responseBody == null) {
            // Then we use the content type from the payload's mime type.
            TypedContent<?> typedContent = message.getContent();
            Type type = typedContent.type();
            MimeType contentType = type.getMimeType();
            return Optional.of(contentType.toString());
        }
        return Optional.empty();
    }

    public static Optional<String> from(String responseBody, Throwable exception) {
        // TODO: Implement me
        return Optional.empty();
    }
}
