package com.esb.foonnel.admin.console.dev.resources;

import com.esb.foonnel.internal.api.InternalAPI;
import com.esb.foonnel.internal.api.module.v1.ModulePUT;
import com.esb.foonnel.internal.api.module.v1.ModuleService;
import org.takes.Request;
import org.takes.Response;
import org.takes.rs.RsWithStatus;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

public class ModulePUTResource extends AbstractModuleMethod {

    ModulePUTResource(ModuleService service) {
        super(service);
    }

    @Override
    public Response act(Request request) throws IOException {
        String json = body(request);
        ModulePUT putRequest = InternalAPI.Module.V1.PUT.deserialize(json);
        service.update(putRequest.getModuleFilePath());

        return new RsWithStatus(HTTP_OK);
    }
}
