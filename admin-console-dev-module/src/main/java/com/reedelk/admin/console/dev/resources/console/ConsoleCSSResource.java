package com.reedelk.admin.console.dev.resources.console;

import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkWithType;

import static com.reedelk.runtime.api.message.type.MimeType.CSS;

public class ConsoleCSSResource extends AbstractConsoleResource {

    private final FkRegex fkRegex;

    public ConsoleCSSResource() {
        this.fkRegex = new FkRegex(BASE_PATH + "/css/.+", new TkWithType(new TkClasspath(), CSS.toString()));
    }

    @Override
    Fork getRoute() {
        return fkRegex;
    }
}
