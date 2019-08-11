package com.reedelk.admin.console.dev.resources.hotswap;

import com.reedelk.runtime.system.api.HotSwapService;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.misc.Opt;

import java.io.IOException;

import static com.reedelk.admin.console.dev.HttpMethod.POST;

public class HotSwapResources implements Fork {

    private final FkRegex fkRegex;

    public HotSwapResources(HotSwapService service, String path) {
        fkRegex = new FkRegex(path, new TkFork(
                new FkMethods(POST.name(), new HotSwapPOSTResource(service))));
    }

    @Override
    public Opt<Response> route(Request request) throws IOException {
        return fkRegex.route(request);
    }
}
