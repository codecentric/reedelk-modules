package com.esb.admin.console.dev.resources.hotswap;

import com.esb.internal.api.HotSwapService;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.misc.Opt;

import java.io.IOException;

import static com.esb.admin.console.dev.HttpMethod.POST;

public class HotSwapResources implements Fork {

    private static final String BASE_PATH = "/hotswap";

    private final FkRegex fkRegex;


    public HotSwapResources(HotSwapService service) {
        fkRegex = new FkRegex(BASE_PATH, new TkFork(
                new FkMethods(POST.name(), new HotSwapPOSTResource(service))));
    }

    @Override
    public Opt<Response> route(Request request) throws IOException {
        return fkRegex.route(request);
    }
}
