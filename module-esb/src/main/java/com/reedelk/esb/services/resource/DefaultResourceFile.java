package com.reedelk.esb.services.resource;

import com.reedelk.runtime.api.resource.ResourceFile;
import org.reactivestreams.Publisher;

public class DefaultResourceFile implements ResourceFile {

    private final Publisher<byte[]> data;
    private final String path;

    DefaultResourceFile(Publisher<byte[]> data, String path) {
        this.data = data;
        this.path = path;
    }

    @Override
    public Publisher<byte[]> data() {
        return data;
    }

    @Override
    public String path() {
        return path;
    }
}
