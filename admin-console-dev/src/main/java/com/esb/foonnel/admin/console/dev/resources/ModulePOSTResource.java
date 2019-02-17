package com.esb.foonnel.admin.console.dev.resources;

import com.esb.foonnel.internal.api.InternalAPI;
import com.esb.foonnel.internal.api.module.v1.ModulePOSTReq;
import com.esb.foonnel.internal.api.module.v1.ModulePOSTRes;
import com.esb.foonnel.internal.api.module.v1.ModuleService;
import org.takes.Request;
import org.takes.Response;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithStatus;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

public class ModulePOSTResource extends AbstractModuleMethod {

    ModulePOSTResource(ModuleService service) {
        super(service);
    }

    @Override
    public Response act(Request request) throws IOException {
        String json = body(request);
        ModulePOSTReq postRequest = InternalAPI.Module.V1.POST.Req.deserialize(json);

        long moduleId = service.installOrUpdate(postRequest.getModuleFilePath());


        ModulePOSTRes dto = new ModulePOSTRes();
        dto.setModuleId(moduleId);

        return new RsWithBody(
                new RsWithStatus(
                        new RsWithHeader("Content-Type", "application/json"), HTTP_OK),
                InternalAPI.Module.V1.POST.Res.serialize(dto));
    }
}
