package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.StringUtils;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;

import java.util.Optional;

public class ContentType {

    public static Optional<String> from(String responseBody, Message message) {
        if (ScriptUtils.isScript(responseBody)) {
            // If it is a script and the script is payload, then we set the content type
            // from the message type
            String unwrappedScript = ScriptUtils.unwrap(responseBody);
            if ("payload".equals(StringUtils.trim(unwrappedScript))) {
                // Then we use the content type from the payload's mime type.
                TypedContent<?> typedContent = message.getContent();
                Type type = typedContent.type();
                MimeType contentType = type.getMimeType();
                return Optional.of(contentType.toString());
            }
        } else {
            // The body is text: if it is not empty, then we set it.
            if (StringUtils.isNotBlank(responseBody)) {
                return Optional.of(MimeType.TEXT.toString());
            }
        }
        // Otherwise we get the content type from the custom headers.
        return Optional.empty();
    }

    public static Optional<String> from(String responseBody, Throwable exception) {
        // TODO: Implement me
        return Optional.empty();
    }
}
