package com.reedelk.file.component;

import com.reedelk.file.commons.MimeTypeParser;
import com.reedelk.file.exception.NotValidFileException;
import com.reedelk.file.read.FileReadConfiguration;
import com.reedelk.file.read.ReadConfiguration;
import com.reedelk.file.read.Reader;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.TypedContentFrom;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.*;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.reactivestreams.Publisher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.reedelk.file.commons.Messages.FileReadComponent.FILE_NAME_ERROR;
import static com.reedelk.file.read.FileReadAttribute.FILE_NAME;
import static com.reedelk.file.read.FileReadAttribute.TIMESTAMP;
import static com.reedelk.runtime.api.commons.ImmutableMap.of;
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
    @Default(MimeType.MIME_TYPE_TEXT_PLAIN)
    @When(propertyName = "autoMimeType", propertyValue = "false")
    @When(propertyName = "autoMimeType", propertyValue = When.BLANK)
    private String mimeType;

    @Property("Configuration")
    private FileReadConfiguration configuration;

    private Reader reader = new Reader();

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        Optional<String> evaluated = service.evaluate(fileName, flowContext, message);

        return evaluated.map(filePath -> {

            MimeType actualMimeType = MimeTypeParser.from(autoMimeType, mimeType, filePath);

            Path path = isBlank(basePath) ? Paths.get(filePath) : Paths.get(basePath, filePath);

            ReadConfiguration config = new ReadConfiguration(configuration);

            Publisher<byte[]> contentAsStream = reader.read(path, config);

            TypedContent<?> content = TypedContentFrom.stream(contentAsStream, actualMimeType);

            MessageAttributes attributes = new DefaultMessageAttributes(FileRead.class,
                    of(FILE_NAME, path.toString(), TIMESTAMP, System.currentTimeMillis()));

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

    public void setAutoMimeType(boolean autoMimeType) {
        this.autoMimeType = autoMimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
