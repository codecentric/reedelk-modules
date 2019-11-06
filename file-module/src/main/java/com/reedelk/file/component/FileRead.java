package com.reedelk.file.component;

import com.reedelk.file.commons.LockType;
import com.reedelk.file.commons.MimeTypeParser;
import com.reedelk.file.commons.ReadFrom;
import com.reedelk.file.commons.ReadOptions;
import com.reedelk.file.configuration.fileread.AdvancedConfiguration;
import com.reedelk.file.exception.NotValidFileException;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.reactivestreams.Publisher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.reedelk.file.commons.Defaults.FileRead.*;
import static com.reedelk.file.commons.Messages.FileReadComponent.FILE_NOT_FOUND_WITH_BASE_PATH;
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

            ReadOptions options = new ReadOptions(getLockType(), getRetryMaxAttempts(), getRetryWaitTime());

            MimeType actualMimeType = MimeTypeParser.from(autoMimeType, mimeType, filePath);

            Publisher<byte[]> contentAsStream;

            if (isBlank(basePath)) {

                Path path = Paths.get(filePath);

                contentAsStream = ReadFrom.path(path, readBufferSize, options);

            } else {

                Path path = Paths.get(basePath, filePath);

                contentAsStream = ReadFrom.path(path, readBufferSize, options);

            }

            TypedContent<byte[]> content = new ByteArrayContent(contentAsStream, actualMimeType);

            return MessageBuilder.get().typedContent(content).build();

        }).orElseThrow(() -> new NotValidFileException(FILE_NOT_FOUND_WITH_BASE_PATH.format(fileName.toString(), basePath)));
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

    private LockType getLockType() {
        return Optional.ofNullable(advancedConfiguration)
                .flatMap(config -> Optional.ofNullable(config.getLockFile()))
                .map(shouldLock -> shouldLock ? LockType.LOCK : LockType.NONE)
                .orElse(LockType.NONE);
    }

    private int getRetryMaxAttempts() {
        return Optional.ofNullable(advancedConfiguration)
                .flatMap(config -> Optional.ofNullable(config.getLockRetryMaxAttempts()))
                .orElse(RETRY_MAX_ATTEMPTS);
    }

    private long getRetryWaitTime() {
        return Optional.ofNullable(advancedConfiguration)
                .flatMap(config -> Optional.ofNullable(config.getLockRetryWaitTime()))
                .orElse(RETRY_WAIT_TIME);
    }
}
