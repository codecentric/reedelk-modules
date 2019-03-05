package com.esb.admin.console.dev;


import org.takes.Request;
import org.takes.misc.Href;
import org.takes.rq.RqHref;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public enum PathParam {

    APP_PATH("appath");

    String name;

    PathParam(String name) {
        this.name = name;
    }

    public String get(Request request) throws IOException {
        Href href = new RqHref.Base(request).href();
        String rawPath;
        try {
            rawPath = new URI(href.bare()).getRawPath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }

        String[] segments = rawPath.split("/");
        return segments[2];
    }

}