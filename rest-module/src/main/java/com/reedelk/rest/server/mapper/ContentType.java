package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;

import java.util.Optional;

public class ContentType {

    public static Optional<String> from(String responseBody, Message message) {
        if (ScriptUtils.isScript(responseBody)) {
            // If it is a script and the script is payload, then we set the content type
            // from the message type
            if (ScriptUtils.isMessagePayload(responseBody)) {
                // Then we use the content type from the payload's mime type.
                return com.reedelk.rest.commons.ContentType.from(message);
            }
        } else {
            // The body is text: if it is not null, then we set it.
            if (StringUtils.isNotNull(responseBody)) {
                return Optional.of(MimeType.TEXT.toString());
            }
        }
        // Otherwise we get the content type from the custom headers.
        return Optional.empty();
    }
}
