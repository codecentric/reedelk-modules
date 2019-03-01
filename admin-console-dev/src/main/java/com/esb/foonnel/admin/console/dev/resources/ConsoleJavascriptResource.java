package com.esb.foonnel.admin.console.dev.resources;

import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkWithType;

public class ConsoleJavascriptResource extends AbstractConsoleResource {

    private final FkRegex fkRegex;

    public ConsoleJavascriptResource() {
        this.fkRegex = new FkRegex(BASE_PATH + "/js/.+", new TkWithType(new TkClasspath(), "text/javascript"));
    }

    @Override
    Fork getRoute() {
        return fkRegex;
    }
}
