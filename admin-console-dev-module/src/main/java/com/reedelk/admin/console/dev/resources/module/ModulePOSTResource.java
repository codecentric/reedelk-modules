package com.reedelk.admin.console.dev.resources.module;

import com.reedelk.admin.console.dev.commons.RequestBody;
import com.reedelk.runtime.rest.api.InternalAPI;
import com.reedelk.runtime.rest.api.module.v1.ModulePOSTReq;
import com.reedelk.runtime.rest.api.module.v1.ModulePOSTRes;
import com.reedelk.runtime.system.api.ModuleService;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithStatus;

import java.io.IOException;

import static com.reedelk.admin.console.dev.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.runtime.api.message.type.MimeType.APPLICATION_JSON;
import static java.net.HttpURLConnection.HTTP_OK;

public class ModulePOSTResource implements Take {

    private final ModuleService service;

    ModulePOSTResource(ModuleService service) {
        this.service = service;
    }

    @Override
    public Response act(Request request) throws IOException {
        String json = RequestBody.from(request);
        ModulePOSTReq postRequest = InternalAPI.Module.V1.POST.Req.deserialize(json);

        long moduleId = service.installOrUpdate(postRequest.getModuleFilePath());

        ModulePOSTRes dto = new ModulePOSTRes();
        dto.setModuleId(moduleId);

        return new RsWithBody(
                new RsWithStatus(
                        new RsWithHeader(CONTENT_TYPE, APPLICATION_JSON.toString()), HTTP_OK),
                InternalAPI.Module.V1.POST.Res.serialize(dto));
    }
}
