package com.esb.admin.console.dev.resources.console;

import org.takes.Request;
import org.takes.Response;
import org.takes.facets.fork.Fork;
import org.takes.misc.Opt;

import java.io.IOException;

abstract class AbstractConsoleResource implements Fork {

    protected static final String BASE_PATH = "/console";

    @Override
    public Opt<Response> route(Request request) throws IOException {
        return getRoute().route(request);
    }

    abstract Fork getRoute();
}
