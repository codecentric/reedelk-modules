package com.reedelk.rest.commons;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;

import java.util.Optional;

public class ContentType {

    private ContentType() {
    }

    public static Optional<String> from(Message message) {
        return Optional.ofNullable(message.getContent())
                .flatMap(typedContent -> Optional.ofNullable(typedContent.type()))
                .flatMap(type -> Optional.ofNullable(type.getMimeType()))
                .map(MimeType::toString);
    }
}
