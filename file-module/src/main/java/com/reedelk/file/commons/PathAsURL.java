package com.reedelk.file.commons;

import com.reedelk.runtime.api.exception.ESBException;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import static java.lang.String.format;

public class PathAsURL {

    public static URL from(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new ESBException(format("Error converting path [%s] to URL", path.toString()), e);
        }
    }
}
