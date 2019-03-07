package com.esb.admin.console.dev.resources.module;

import com.esb.internal.api.InternalAPI;
import com.esb.internal.api.module.v1.ModuleGETRes;
import com.esb.internal.api.module.v1.ModulesGETRes;
import com.esb.system.api.ModuleService;
import com.esb.system.api.Modules;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;

import java.io.IOException;
import java.util.List;

import static com.esb.admin.console.dev.commons.HttpHeader.CONTENT_TYPE;
import static com.esb.api.message.MimeType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;

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
        Modules modules = service.modules();

        List<ModuleGETRes> dtos = modules.getModules()
                .stream().map(module -> {
                    ModuleGETRes dto = new ModuleGETRes();
                    dto.setName(module.getName());
                    dto.setState(module.getState());
                    dto.setErrors(module.getErrors());
                    dto.setVersion(module.getVersion());
                    dto.setModuleId(module.getModuleId());
                    dto.setModuleFilePath(module.getModuleFilePath());
                    dto.setResolvedComponents(module.getResolvedComponents());
                    dto.setUnresolvedComponents(module.getUnresolvedComponents());
                    return dto;
                })
                .collect(toList());

        ModulesGETRes response = new ModulesGETRes();
        response.setModules(dtos);
        return InternalAPI.Module.V1.GET.Res.serialize(response).getBytes();
    }
}
