package com.esb.foonnel.admin.console.dev;

import com.esb.foonnel.domain.DeploymentService;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.misc.Opt;

import java.io.IOException;

import static com.esb.foonnel.admin.console.dev.GenericHandler.handlerFor;
import static com.esb.foonnel.admin.console.dev.HttpMethod.*;

public class DeploymentResources implements Fork {

    private static final String BASE_PATH = "/extension";

    private final FkRegex fkRegex;

    public DeploymentResources(DeploymentService service) {
        fkRegex = new FkRegex(BASE_PATH + "/.*", new TkFork(
                new FkMethods(PUT.name(), handlerFor(service::update)),
                new FkMethods(POST.name(), handlerFor(service::installOrUpdate)),
                new FkMethods(DELETE.name(), handlerFor(service::uninstall))));
    }

    @Override
    public Opt<Response> route(Request request) throws IOException {
        return fkRegex.route(request);
    }
}
