package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;

import java.util.Optional;

public class ContentType {

    public static Optional<String> from(DynamicByteArray responseBody, Message message) {
        if (responseBody != null && responseBody.isScript()) {
            // If it is a script and the script is evaluate the payload, then we set the
            // content type from the payload's mime type.
            if (responseBody.isEvaluateMessagePayload()) {
                return com.reedelk.rest.commons.ContentType.from(message);
            }
        }
        // Otherwise we get the content type from the custom headers.
        return Optional.empty();
    }
}
