package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import com.reedelk.runtime.api.resource.ResourceService;
import com.reedelk.runtime.commons.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.FileNotFoundException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Local file read")
@Component(service = LocalFileRead.class, scope = PROTOTYPE)
public class LocalFileRead implements ProcessorSync {

    @Property("File to load")
    private ResourceDynamic fileName;

    @Reference
    private ResourceService resourceService;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        try {
            ResourceService.ResourceFile resourceFile =
                    resourceService.findResourceBy(fileName, flowContext, message);

            String pageFileExtension = FileUtils.getExtension(resourceFile.filePath());

            MimeType actualMimeType = MimeType.fromFileExtension(pageFileExtension);

            TypedContent<byte[]> content = new ByteArrayContent(resourceFile.data(), actualMimeType);

            return MessageBuilder.get().typedContent(content).build();

        } catch (FileNotFoundException e) {
            throw new ESBException(e);
        }
    }

    public void setFileName(ResourceDynamic fileName) {
        this.fileName = fileName;
    }
}
