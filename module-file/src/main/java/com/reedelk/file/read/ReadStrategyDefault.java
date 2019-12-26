package com.reedelk.file.read;

import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;

import java.nio.file.Path;

public class ReadStrategyDefault extends ReadStrategyStream {

    @Override
    public TypedContent<?> read(Path path, ReadConfigurationDecorator config, MimeType actualMimeType) {
        TypedContent<?> read = super.read(path, config, actualMimeType);
        // We immediately consume the content.
        read.consume();
        return read;
    }
}
