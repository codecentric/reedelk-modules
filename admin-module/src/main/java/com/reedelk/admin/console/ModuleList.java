package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.rest.api.InternalAPI;
import com.reedelk.runtime.rest.api.module.v1.ModuleGETRes;
import com.reedelk.runtime.rest.api.module.v1.ModulesGETRes;
import com.reedelk.runtime.system.api.ModuleService;
import com.reedelk.runtime.system.api.Modules;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;


@ESBComponent("List modules")
@Component(service = ModuleList.class, scope = PROTOTYPE)
public class ModuleList implements ProcessorSync {

    @Reference
    private ModuleService service;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        String modulesAsJson = modules();
        return MessageBuilder.get().json(modulesAsJson).build();
    }

    private String modules() {
        Modules modules = service.modules();

        List<ModuleGETRes> modulesDTOs = modules.getModules()
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
                }).collect(toList());

        ModulesGETRes response = new ModulesGETRes();
        response.setModules(modulesDTOs);
        return InternalAPI.Module.V1.GET.Res.serialize(response);
    }

}
