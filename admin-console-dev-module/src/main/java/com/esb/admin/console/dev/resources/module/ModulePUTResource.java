package com.esb.admin.console.dev.resources.module;

import com.esb.admin.console.dev.commons.RequestBody;
import com.esb.internal.rest.api.InternalAPI;
import com.esb.internal.rest.api.module.v1.ModulePUTReq;
import com.esb.internal.rest.api.module.v1.ModulePUTRes;
import com.esb.system.api.ModuleService;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithStatus;

import java.io.IOException;

import static com.esb.admin.console.dev.commons.HttpHeader.CONTENT_TYPE;
import static com.esb.api.message.MimeType.APPLICATION_JSON;
import static java.net.HttpURLConnection.HTTP_OK;

public class ModulePUTResource implements Take {

    private final ModuleService service;

    ModulePUTResource(ModuleService service) {
        this.service = service;
    }

    @Override
    public Response act(Request request) throws IOException {
        String json = RequestBody.from(request);
        ModulePUTReq putRequest = InternalAPI.Module.V1.PUT.Req.deserialize(json);
        long moduleId = service.update(putRequest.getModuleFilePath());

        ModulePUTRes dto = new ModulePUTRes();
        dto.setModuleId(moduleId);

        return new RsWithBody(
                new RsWithStatus(
                        new RsWithHeader(CONTENT_TYPE, APPLICATION_JSON.toString()), HTTP_OK),
                InternalAPI.Module.V1.PUT.Res.serialize(dto));
    }
}
