package com.reedelk.file.localread;

import java.util.Optional;

import static com.reedelk.file.commons.Defaults.LocalFileRead.READ_FILE_BUFFER_SIZE;

public class LocalReadConfiguration {

    private final int readBufferSize;

    public LocalReadConfiguration(LocalFileReadConfiguration configuration) {
        this.readBufferSize = getReadBufferSize(configuration);
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    private int getReadBufferSize(LocalFileReadConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getReadBufferSize()))
                .orElse(READ_FILE_BUFFER_SIZE);
    }
}
