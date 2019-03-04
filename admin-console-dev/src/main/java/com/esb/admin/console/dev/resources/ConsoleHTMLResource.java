package com.esb.admin.console.dev.resources;

import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkWithType;

public class ConsoleHTMLResource extends AbstractConsoleResource {

    private final FkRegex fkRegex;

    public ConsoleHTMLResource() {
        this.fkRegex = new FkRegex(BASE_PATH + "/html/.+", new TkWithType(new TkClasspath(), "text/html"));
    }

    @Override
    Fork getRoute() {
        return fkRegex;
    }

}
