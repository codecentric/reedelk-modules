package com.reedelk.file.component;

import com.reedelk.file.commons.FileReadAttribute;
import com.reedelk.file.commons.MimeTypeParser;
import com.reedelk.file.configuration.localfileread.AdvancedConfiguration;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ModuleFileNotFoundException;
import com.reedelk.runtime.api.file.ModuleFileProvider;
import com.reedelk.runtime.api.file.ModuleId;
import com.reedelk.runtime.api.message.*;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;

import java.nio.file.Paths;
import java.util.Optional;

import static com.reedelk.file.commons.Defaults.LocalFileRead.READ_FILE_BUFFER_SIZE;
import static com.reedelk.file.commons.Messages.ModuleFileReadComponent.FILE_NOT_FOUND;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Local file read")
@Component(service = LocalFileRead.class, scope = PROTOTYPE)
public class LocalFileRead implements ProcessorSync {

    @Reference
    private ScriptEngineService service;
    @Reference
    private ModuleFileProvider moduleFileProvider;

    @Hidden
    @Property("Module Id")
    private ModuleId moduleId;

    @Property("File name")
    private DynamicString fileName;

    @Property("Base path")
    private String basePath;

    @Property("Auto mime type")
    @Default("true")
    private boolean autoMimeType;

    @Property("Mime type")
    @MimeTypeCombo
    @Default(MimeType.ANY_MIME_TYPE)
    @When(propertyName = "autoMimeType", propertyValue = "false")
    @When(propertyName = "autoMimeType", propertyValue = When.BLANK)
    private String mimeType;

    @Property("Advanced configuration")
    private AdvancedConfiguration advancedConfiguration;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        Optional<String> evaluated = service.evaluate(fileName, message, flowContext);

        return evaluated.map(filePath -> {

            int readBufferSize = getReadBufferSize();

            MimeType actualMimeType = MimeTypeParser.from(autoMimeType, mimeType, filePath);;

            Publisher<byte[]> contentAsStream;

            String finalFilePath = filePath;

            if (isBlank(basePath)) {

                contentAsStream = moduleFileProvider.findBy(moduleId, finalFilePath, readBufferSize);

            } else {

                finalFilePath = Paths.get(basePath, finalFilePath).toString();

                contentAsStream = moduleFileProvider.findBy(moduleId, finalFilePath, readBufferSize);
            }

            TypedContent<byte[]> content = new ByteArrayContent(contentAsStream, actualMimeType);

            MessageAttributes attributes = new DefaultMessageAttributes(ImmutableMap.of(
                    FileReadAttribute.FILE_NAME, finalFilePath,
                    FileReadAttribute.TIMESTAMP, System.currentTimeMillis()));

            return MessageBuilder.get().attributes(attributes).typedContent(content).build();

        }).orElseThrow(() -> new ModuleFileNotFoundException(FILE_NOT_FOUND.format(fileName.toString(), basePath, moduleId.get())));
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }

    public void setModuleId(ModuleId moduleId) {
        this.moduleId = moduleId;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setAdvancedConfiguration(AdvancedConfiguration advancedConfiguration) {
        this.advancedConfiguration = advancedConfiguration;
    }

    public void setAutoMimeType(boolean autoMimeType) {
        this.autoMimeType = autoMimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    private int getReadBufferSize() {
        return Optional.ofNullable(advancedConfiguration)
                .flatMap(config -> Optional.ofNullable(config.getReadBufferSize()))
                .orElse(READ_FILE_BUFFER_SIZE);
    }
}
