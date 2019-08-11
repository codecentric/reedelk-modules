package com.reedelk.admin.console.dev.resources.console;

import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkWithType;

import static com.reedelk.runtime.api.message.type.MimeType.JAVASCRIPT;

public class ConsoleJavascriptResource extends AbstractConsoleResource {

    private final FkRegex fkRegex;

    public ConsoleJavascriptResource() {
        this.fkRegex = new FkRegex(BASE_PATH + "/js/.+", new TkWithType(new TkClasspath(), JAVASCRIPT.toString()));
    }

    @Override
    Fork getRoute() {
        return fkRegex;
    }
}
