package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.rest.api.InternalAPI;
import com.reedelk.runtime.rest.api.module.v1.ModulePOSTReq;
import com.reedelk.runtime.rest.api.module.v1.ModulePOSTRes;
import com.reedelk.runtime.system.api.ModuleService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;


@ESBComponent("Install or update module")
@Component(service = ModuleInstallOrUpdate.class, scope = PROTOTYPE)
public class ModuleInstallOrUpdate implements ProcessorSync {

    private static final Logger logger = LoggerFactory.getLogger(ModuleInstallOrUpdate.class);

    @Reference
    private ModuleService service;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String payload = message.payload();

        logger.info("Payload: " + payload);

        String resultJson = installOrUpdate(payload);

        return MessageBuilder.get().json(resultJson).build();
    }

    private String installOrUpdate(String json) {
        ModulePOSTReq postRequest = InternalAPI.Module.V1.POST.Req.deserialize(json);

        long moduleId = service.installOrUpdate(postRequest.getModuleFilePath());

        ModulePOSTRes dto = new ModulePOSTRes();
        dto.setModuleId(moduleId);

        return InternalAPI.Module.V1.POST.Res.serialize(dto);
    }
}
