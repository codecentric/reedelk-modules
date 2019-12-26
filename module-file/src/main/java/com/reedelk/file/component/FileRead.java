package com.reedelk.file.component;

import com.reedelk.file.commons.MimeTypeParser;
import com.reedelk.file.exception.NotValidFileException;
import com.reedelk.file.read.*;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.*;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.reedelk.file.commons.Messages.FileReadComponent.FILE_NAME_ERROR;
import static com.reedelk.file.read.FileReadAttribute.FILE_NAME;
import static com.reedelk.file.read.FileReadAttribute.TIMESTAMP;
import static com.reedelk.runtime.api.commons.ImmutableMap.of;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

@ESBComponent("File Read")
@Component(service = FileRead.class, scope = ServiceScope.PROTOTYPE)
public class FileRead implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @Property("File name")
    private DynamicString fileName;

    @Property("Base path")
    private String basePath;

    @Property("Read mode")
    @Default("DEFAULT")
    private ReadMode mode;

    @Property("Auto mime type")
    @Default("true")
    private boolean autoMimeType;

    @Property("Mime type")
    @MimeTypeCombo
    @Default(MimeType.MIME_TYPE_TEXT_PLAIN)
    @When(propertyName = "autoMimeType", propertyValue = "false")
    @When(propertyName = "autoMimeType", propertyValue = When.BLANK)
    private String mimeType;

    @Property("Configuration")
    private FileReadConfiguration configuration;

    private ReadStrategy strategy;

    @Override
    public void initialize() {
        strategy = ReadMode.STREAM.equals(mode) ?
                new ReadStrategyStream() :
                new ReadStrategyDefault();
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        Optional<String> evaluated = service.evaluate(fileName, flowContext, message);

        return evaluated.map(filePath -> {

            MimeType actualMimeType = MimeTypeParser.from(autoMimeType, mimeType, filePath);

            Path path = isBlank(basePath) ? Paths.get(filePath) : Paths.get(basePath, filePath);

            ReadConfigurationDecorator config = new ReadConfigurationDecorator(configuration);

            MessageAttributes attributes = new DefaultMessageAttributes(FileRead.class,
                    of(FILE_NAME, path.toString(), TIMESTAMP, System.currentTimeMillis()));

            TypedContent<?> content = strategy.read(path, config, actualMimeType);

            return MessageBuilder.get().attributes(attributes).typedContent(content).build();

        }).orElseThrow(() -> new NotValidFileException(FILE_NAME_ERROR.format(fileName.toString())));
    }

    public void setConfiguration(FileReadConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setMode(ReadMode mode) {
        this.mode = mode;
    }

    public void setAutoMimeType(boolean autoMimeType) {
        this.autoMimeType = autoMimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
