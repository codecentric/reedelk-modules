package com.esb.foonnel.admin.console.dev.resources;

import com.esb.foonnel.internal.api.InternalAPI;
import com.esb.foonnel.internal.api.module.v1.ModuleService;
import com.esb.foonnel.internal.api.module.v1.ModulesGET;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;

import java.io.IOException;

public class ModuleGETResource implements Take {

    private final ModuleService service;

    public ModuleGETResource(ModuleService service) {
        this.service = service;
    }

    @Override
    public Response act(Request request) throws IOException {
        return new RsWithBody(new RsWithHeader("Content-Type: application/json"), servicestatus(service));
    }

    private byte[] servicestatus(ModuleService service) {
        ModulesGET modules = service.modules();
        return InternalAPI.Module.V1.GET.serialize(modules).getBytes();
    }
}
