package com.esb.admin.console.dev.resources.module;

import com.esb.internal.api.InternalAPI;
import com.esb.internal.api.ModuleService;
import com.esb.internal.api.module.v1.ModulesGETRes;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;

import java.io.IOException;

import static com.esb.admin.console.dev.commons.HttpHeader.CONTENT_TYPE;
import static com.esb.api.message.MimeType.APPLICATION_JSON;


public class ModuleGETResource implements Take {

    private final ModuleService service;

    ModuleGETResource(ModuleService service) {
        this.service = service;
    }

    @Override
    public Response act(Request request) throws IOException {
        return new RsWithBody(
                new RsWithHeader(CONTENT_TYPE, APPLICATION_JSON.toString()), servicestatus(service));
    }

    private byte[] servicestatus(ModuleService service) {
        ModulesGETRes modules = service.modules();
        return InternalAPI.Module.V1.GET.Res.serialize(modules).getBytes();
    }
}
