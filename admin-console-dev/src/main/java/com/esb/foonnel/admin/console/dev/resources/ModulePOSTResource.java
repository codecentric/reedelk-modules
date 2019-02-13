package com.esb.foonnel.admin.console.dev.resources;

import com.esb.foonnel.internal.api.InternalAPI;
import com.esb.foonnel.internal.api.module.v1.ModulePOST;
import com.esb.foonnel.internal.api.module.v1.ModuleService;
import org.takes.Request;
import org.takes.Response;
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
        ModulePOST postRequest = InternalAPI.Module.V1.POST.deserialize(json);
        service.installOrUpdate(postRequest.getModuleFilePath());

        return new RsWithStatus(HTTP_OK);
    }
}
