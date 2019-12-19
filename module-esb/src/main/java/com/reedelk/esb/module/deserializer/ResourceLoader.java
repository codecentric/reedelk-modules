package com.reedelk.esb.module.deserializer;

import java.net.URL;

import static com.reedelk.runtime.commons.FileUtils.ReadFromURL;

// Lazy Loading of resources
public class ResourceLoader {

    private final URL resourceURL;

    public ResourceLoader(URL resourceURL) {
        this.resourceURL = resourceURL;
    }

    public String getResourceFilePath() {
        return resourceURL.getPath();
    }

    public String bodyAsString() {
        return ReadFromURL.asString(resourceURL);
    }

    public byte[] bodyAsBytes() {
        return ReadFromURL.asByteArray(resourceURL);
    }
}