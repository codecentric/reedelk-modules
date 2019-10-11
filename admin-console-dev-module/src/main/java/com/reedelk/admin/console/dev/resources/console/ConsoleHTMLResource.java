package com.reedelk.admin.console.dev.resources.console;

import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkWithType;

import static com.reedelk.runtime.api.message.content.MimeType.HTML;

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
