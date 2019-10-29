package com.reedelk.core.component.filesystem;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.file.ModuleFileProvider;
import com.reedelk.runtime.api.file.ModuleId;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.commons.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Local file read")
@Component(service = LocalFileReadComponent.class, scope = PROTOTYPE)
public class LocalFileReadComponent implements ProcessorSync {

    @Reference
    private ScriptEngineService service;
    @Reference
    private ModuleFileProvider moduleFileProvider;

    @Hidden
    @Property("Module Id")
    private ModuleId moduleId;

    @Property("File name")
    private DynamicString fileName;

    @Property("Auto mime type")
    @Default("true")
    private boolean autoMimeType;

    @Property("Mime type")
    @MimeTypeCombo
    @Default(MimeType.ANY_MIME_TYPE)
    @When(propertyName = "autoMimeType", propertyValue = "false")
    @When(propertyName = "autoMimeType", propertyValue = When.BLANK)
    private String mimeType;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        Optional<String> evaluated = service.evaluate(fileName, message, flowContext);

        if (evaluated.isPresent()) {

            String file = evaluated.get();

            byte[] fileData = moduleFileProvider.findBy(moduleId, file);

            MimeType actualMimeType;

            if (autoMimeType) {

                String pageFileExtension = FileUtils.getExtension(file);

                actualMimeType = MimeType.fromFileExtension(pageFileExtension);

            } else {
                actualMimeType = MimeType.parse(mimeType);
            }

            TypedContent<byte[]> content = new ByteArrayContent(fileData, actualMimeType);

            return MessageBuilder.get().typedContent(content).build();
        }

        return MessageBuilder.get().empty().build();
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }

    public void setModuleId(ModuleId moduleId) {
        this.moduleId = moduleId;
    }

    public void setAutoMimeType(boolean autoMimeType) {
        this.autoMimeType = autoMimeType;
    }
}
