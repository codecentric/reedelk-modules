package com.esb.admin.console.dev.resources.console;

import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.tk.TkHtml;
import org.takes.tk.TkWithType;

import java.io.InputStream;

import static com.esb.api.message.MimeType.HTML;

public class ConsoleIndexResource extends AbstractConsoleResource {

    @Override
    Fork getRoute() {
        InputStream input = this.getClass().getResourceAsStream("/console/index.html");
        return new FkRegex(BASE_PATH, new TkWithType(new TkHtml(input), HTML.toString()));
    }

}
