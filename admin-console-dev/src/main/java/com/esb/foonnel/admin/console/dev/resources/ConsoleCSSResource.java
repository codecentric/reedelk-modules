package com.esb.foonnel.admin.console.dev.resources;

import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkWithType;

public class ConsoleCSSResource extends AbstractConsoleResource {

    private final FkRegex fkRegex;

    public ConsoleCSSResource() {
        this.fkRegex = new FkRegex(BASE_PATH + "/css/.+", new TkWithType(new TkClasspath(), "text/css"));
    }

    @Override
    Fork getRoute() {
        return fkRegex;
    }
}
