package com.reedelk.file.read;

import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;

import java.nio.file.Path;

public interface ReadStrategy {
    TypedContent<?> read(Path path, ReadConfigurationDecorator config, MimeType actualMimeType);
}
