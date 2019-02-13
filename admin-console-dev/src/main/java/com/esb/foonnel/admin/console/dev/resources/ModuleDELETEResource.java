package com.esb.foonnel.admin.console.dev.resources;

import com.esb.foonnel.internal.api.InternalAPI;
import com.esb.foonnel.internal.api.module.v1.ModuleDELETE;
import com.esb.foonnel.internal.api.module.v1.ModuleService;
import org.takes.Request;
import org.takes.Response;
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
        ModuleDELETE deleteRequest = InternalAPI.Module.V1.DELETE.deserialize(json);
        service.uninstall(deleteRequest.getModuleFilePath());

        return new RsWithStatus(HTTP_OK);
    }
}
