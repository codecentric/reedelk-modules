package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.Part;
import com.reedelk.runtime.api.message.content.Parts;
import com.reedelk.runtime.system.api.ModuleService;
import com.reedelk.runtime.system.api.SystemProperty;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Deploy a module")
@Component(service = ModuleDeploy.class, scope = PROTOTYPE)
public class ModuleDeploy implements ProcessorSync {

    @Reference
    private SystemProperty systemProperty;
    @Reference
    private ModuleService service;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String modulesDirectory = systemProperty.modulesDirectory();

        Parts parts = message.payload();
        Part filename = parts.get("filename");

        String finalName = ""; // modulesDirectory + something;
        //service.installOrUpdate("");

        return MessageBuilder.get().build();
    }
}
