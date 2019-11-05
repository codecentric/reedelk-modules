package com.reedelk.file.component;

import com.reedelk.file.commons.MimeTypeParser;
import com.reedelk.file.configuration.fileread.AdvancedConfiguration;
import com.reedelk.file.exception.FileNotFoundException;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.commons.PublisherFrom;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.reactivestreams.Publisher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.reedelk.file.commons.Defaults.FileRead.READ_FILE_BUFFER_SIZE;
import static com.reedelk.file.commons.Messages.FileReadComponent.FILE_NOT_FOUND;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

@ESBComponent("File read")
@Component(service = FileRead.class, scope = ServiceScope.PROTOTYPE)
public class FileRead implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

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

            MimeType actualMimeType = MimeTypeParser.from(autoMimeType, mimeType, filePath);

            Publisher<byte[]> contentAsStream;

            if (isBlank(basePath)) {

                Path path = Paths.get(filePath);

                contentAsStream = PublisherFrom.path(path, readBufferSize);

            } else {

                Path path = Paths.get(basePath, filePath);

                contentAsStream = PublisherFrom.path(path, readBufferSize);

            }

            TypedContent<byte[]> content = new ByteArrayContent(contentAsStream, actualMimeType);

            return MessageBuilder.get().typedContent(content).build();

        }).orElseThrow(() -> new FileNotFoundException(FILE_NOT_FOUND.format(fileName.toString(), basePath)));
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }

    public void setAutoMimeType(boolean autoMimeType) {
        this.autoMimeType = autoMimeType;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setAdvancedConfiguration(AdvancedConfiguration advancedConfiguration) {
        this.advancedConfiguration = advancedConfiguration;
    }

    private int getReadBufferSize() {
        return Optional.ofNullable(advancedConfiguration)
                .flatMap(config -> Optional.ofNullable(config.getReadBufferSize()))
                .orElse(READ_FILE_BUFFER_SIZE);
    }
}
