package com.esb.module.deserializer;

import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class BundleDeserializer extends AbstractDeserializer {

    private final Bundle bundle;

    public BundleDeserializer(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    protected List<URL> getResources(String directory) {
        Enumeration<String> entryPaths = bundle.getEntryPaths(directory);
        if (entryPaths == null) return Collections.emptyList();
        return Collections
                .list(entryPaths)
                .stream()
                .map(bundle::getResource)
                .collect(toList());
    }

}
