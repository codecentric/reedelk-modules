package com.esb.admin.console.dev.resources;

import com.esb.internal.api.InternalAPI;
import com.esb.internal.api.module.v1.ModuleDELETEReq;
import com.esb.internal.api.module.v1.ModuleDELETERes;
import com.esb.internal.api.module.v1.ModuleService;
import org.takes.Request;
import org.takes.Response;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithStatus;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

public class ModuleDELETEResource extends AbstractModuleMethod {

    ModuleDELETEResource(ModuleService service) {
        super(service);
    }

    @Override
    public Response act(Request request) throws IOException {
        String json = body(request);
        ModuleDELETEReq deleteRequest = InternalAPI.Module.V1.DELETE.Req.deserialize(json);

        long moduleId = service.uninstall(deleteRequest.getModuleFilePath());

        ModuleDELETERes dto = new ModuleDELETERes();
        dto.setModuleId(moduleId);

        return new RsWithBody(
                new RsWithStatus(
                        new RsWithHeader("Content-Type", "application/json"), HTTP_OK),
                InternalAPI.Module.V1.DELETE.Res.serialize(dto));
    }
}
