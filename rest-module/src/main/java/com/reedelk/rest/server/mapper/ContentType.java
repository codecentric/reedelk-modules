package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.script.DynamicValue;

import java.util.Optional;

public class ContentType {

    public static Optional<String> from(DynamicValue responseBody, Message message) {
        if (responseBody == null) {
            return Optional.empty();

        } else if (responseBody.isScript()) {
            // If it is a script and the script is payload, then we set the content type
            // from the message type
            if (responseBody.isMessagePayload()) {
                // Then we use the content type from the payload's mime type.
                return com.reedelk.rest.commons.ContentType.from(message);
            }
        } else if (responseBody.isNotNull()) {
            // The body is text: if it is not null, then we set it.
            return Optional.of(MimeType.TEXT.toString());
        }
        // Otherwise we get the content type from the custom headers.
        return Optional.empty();
    }
}
