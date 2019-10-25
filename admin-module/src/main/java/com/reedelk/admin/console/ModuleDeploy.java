package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.Part;
import com.reedelk.runtime.api.message.content.Parts;
import com.reedelk.runtime.system.api.ModuleService;
import com.reedelk.runtime.system.api.SystemProperty;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Module Deploy")
@Component(service = ModuleDeploy.class, scope = PROTOTYPE)
public class ModuleDeploy implements ProcessorSync {

    private static final Logger logger = LoggerFactory.getLogger(ModuleDeploy.class);

    @Reference
    private SystemProperty systemProperty;
    @Reference
    private ModuleService service;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String modulesDirectory = systemProperty.modulesDirectory();

        logger.info("Modules directory: " + modulesDirectory);

        Parts parts = message.payload();
        Part part = parts.get("moduleFilePath");

        // TODO: Check that part is not null
        String jarFileName = part.getAttributes().get("filename");
        byte[] jarArchiveBytes = (byte[]) part.getContent().data();

        Path finalName = Paths.get(modulesDirectory, jarFileName);

        logger.info("Module file final name: " + finalName);

        try (FileOutputStream fos = new FileOutputStream(finalName.toString())) {
            fos.write(jarArchiveBytes);
        } catch (IOException e) {
            throw new ESBException(e);
        }

        try {
            String pathAsUri = finalName.toUri().toURL().toString();
            service.installOrUpdate(pathAsUri);
            return MessageBuilder.get().build();
        } catch (MalformedURLException e) {
            throw new ESBException(e);
        }
    }
}
