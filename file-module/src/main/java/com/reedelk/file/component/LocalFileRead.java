package com.reedelk.file.component;

import com.reedelk.file.commons.MimeTypeParser;
import com.reedelk.file.localread.LocalFileReadConfiguration;
import com.reedelk.file.localread.LocalReadConfiguration;
import com.reedelk.runtime.api.annotation.*;
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

import static com.reedelk.file.commons.Messages.ModuleFileReadComponent.FILE_NOT_FOUND;
import static com.reedelk.file.localread.LocalFileReadAttribute.FILE_NAME;
import static com.reedelk.file.localread.LocalFileReadAttribute.TIMESTAMP;
import static com.reedelk.runtime.api.commons.ImmutableMap.of;
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

    @Property("Configuration")
    private LocalFileReadConfiguration configuration;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        Optional<String> evaluated = service.evaluate(fileName, message, flowContext);

        return evaluated.map(filePath -> {

            LocalReadConfiguration config = new LocalReadConfiguration(configuration);

            MimeType actualMimeType = MimeTypeParser.from(autoMimeType, mimeType, filePath);

            String finalFilePath = isBlank(basePath) ? filePath : Paths.get(basePath, filePath).toString();

            Publisher<byte[]> contentAsStream = moduleFileProvider.findBy(moduleId, finalFilePath, config.getReadBufferSize());

            TypedContent<byte[]> content = new ByteArrayContent(contentAsStream, actualMimeType);

            MessageAttributes attributes = new DefaultMessageAttributes(LocalFileRead.class,
                    of(FILE_NAME, finalFilePath, TIMESTAMP, System.currentTimeMillis()));

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

    public void setConfiguration(LocalFileReadConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setAutoMimeType(boolean autoMimeType) {
        this.autoMimeType = autoMimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
