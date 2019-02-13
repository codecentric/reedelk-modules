package com.esb.foonnel.admin.console.dev.resources;

import com.esb.foonnel.internal.api.API;
import com.esb.foonnel.internal.api.module.v1.ModuleService;
import com.esb.foonnel.internal.api.module.v1.ModulesGET;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.misc.Opt;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;

import java.io.IOException;

import static com.esb.foonnel.admin.console.dev.resources.GenericHandler.handlerFor;
import static com.esb.foonnel.admin.console.dev.HttpMethod.*;

public class ModuleResources implements Fork {

    private static final String BASE_PATH = "/module";

    private final FkRegex fkRegex;

    public ModuleResources(ModuleService service) {
        fkRegex = new FkRegex(BASE_PATH + "/.*", new TkFork(
                new FkMethods(PUT.name(), handlerFor(service::update)),
                new FkMethods(POST.name(), handlerFor(service::installOrUpdate)),
                new FkMethods(DELETE.name(), handlerFor(service::uninstall)),
                new FkMethods(GET.name(), request -> new RsWithBody(new RsWithHeader("Content-Type: application/json"), servicestatus(service)))
        ));
    }

    @Override
    public Opt<Response> route(Request request) throws IOException {
        return fkRegex.route(request);
    }

    private byte[] servicestatus(ModuleService service) {
        ModulesGET modules = service.modules();
        return API.Module.V1.GET.serializer().serialize(modules).getBytes();
    }

}
