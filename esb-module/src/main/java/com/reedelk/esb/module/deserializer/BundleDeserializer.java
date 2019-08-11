package com.reedelk.esb.module.deserializer;

import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class BundleDeserializer extends AbstractDeserializer {

    private static final boolean RECURSIVE = true;

    private final Bundle bundle;

    public BundleDeserializer(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    protected List<URL> getResources(String directory, String suffix) {
        Enumeration<URL> entryPaths = bundle.findEntries(directory, "*." + suffix, RECURSIVE);
        return entryPaths == null ?
                Collections.emptyList() :
                Collections.list(entryPaths);
    }

}
