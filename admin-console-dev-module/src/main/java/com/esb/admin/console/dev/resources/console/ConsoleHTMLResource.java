package com.esb.admin.console.dev.resources.console;

import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkWithType;

import static com.esb.api.message.type.MimeType.HTML;

public class ConsoleHTMLResource extends AbstractConsoleResource {

    private final FkRegex fkRegex;

    public ConsoleHTMLResource() {
        this.fkRegex = new FkRegex(BASE_PATH + "/html/.+", new TkWithType(new TkClasspath(), HTML.toString()));
    }

    @Override
    Fork getRoute() {
        return fkRegex;
    }

}
