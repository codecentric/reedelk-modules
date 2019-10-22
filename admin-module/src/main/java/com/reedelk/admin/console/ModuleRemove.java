package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.rest.api.InternalAPI;
import com.reedelk.runtime.rest.api.module.v1.ModuleDELETEReq;
import com.reedelk.runtime.rest.api.module.v1.ModuleDELETERes;
import com.reedelk.runtime.system.api.ModuleService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Remove module")
@Component(service = ModuleRemove.class, scope = PROTOTYPE)
public class ModuleRemove implements ProcessorSync {

    @Reference
    private ModuleService service;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String payload = message.payload();

        String resultJson = delete(payload);

        return MessageBuilder.get().json(resultJson).build();
    }

    private String delete(String json) {

        ModuleDELETEReq deleteRequest = InternalAPI.Module.V1.DELETE.Req.deserialize(json);

        long moduleId = service.uninstall(deleteRequest.getModuleFilePath());

        ModuleDELETERes dto = new ModuleDELETERes();

        dto.setModuleId(moduleId);

        return InternalAPI.Module.V1.DELETE.Res.serialize(dto);
    }
}
