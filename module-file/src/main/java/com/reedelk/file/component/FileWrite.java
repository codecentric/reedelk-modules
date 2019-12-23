package com.reedelk.file.component;

import com.reedelk.file.exception.FileWriteException;
import com.reedelk.file.write.FileWriteConfiguration;
import com.reedelk.file.write.WriteConfiguration;
import com.reedelk.file.write.WriteMode;
import com.reedelk.file.write.Writer;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.reedelk.file.commons.Messages.FileWriteComponent.ERROR_FILE_WRITE;
import static com.reedelk.file.commons.Messages.FileWriteComponent.ERROR_FILE_WRITE_WITH_PATH;
import static com.reedelk.runtime.api.commons.StackTraceUtils.rootCauseMessageOf;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

@ESBComponent("File Write")
@Component(service = FileWrite.class, scope = ServiceScope.PROTOTYPE)
public class FileWrite implements ProcessorAsync {

    @Reference
    private ScriptEngineService scriptService;
    @Reference
    private ConverterService converterService;

    @Property("File name")
    private DynamicString filePath;

    @Property("Base path")
    private String basePath;

    @Property("Write mode")
    private WriteMode mode;

    @Property("Configuration")
    private FileWriteConfiguration configuration;

    private final Writer writer = new Writer();

    @Override
    public void apply(Message message, FlowContext flowContext, OnResult callback) {


        Optional<String> evaluated = scriptService.evaluate(filePath, flowContext, message);

        if (evaluated.isPresent()) {

            Path finalPath = null;

            try {

                WriteConfiguration config = new WriteConfiguration(configuration, mode);

                String filePath = evaluated.get();

                finalPath = isBlank(basePath) ? Paths.get(filePath) : Paths.get(basePath, filePath);

                if (config.isCreateParentDirectory()) {
                    Files.createDirectories(finalPath.getParent());
                }

                TypedPublisher<?> originalStream = message.content().stream();

                TypedPublisher<byte[]> originalStreamAsBytes = converterService.convert(originalStream, byte[].class);

                writer.write(config, flowContext, callback, finalPath, originalStreamAsBytes);

            } catch (Exception exception) {
                String errorMessage = finalPath != null ?
                        ERROR_FILE_WRITE_WITH_PATH.format(finalPath.toString(), rootCauseMessageOf(exception)) :
                        ERROR_FILE_WRITE.format(rootCauseMessageOf(exception));
                callback.onError(new FileWriteException(errorMessage, exception), flowContext);
            }

        } else {
            callback.onError(new ESBException("Could not write file"), flowContext);
        }
    }

    public void setFilePath(DynamicString filePath) {
        this.filePath = filePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setMode(WriteMode mode) {
        this.mode = mode;
    }

    public void setConfiguration(FileWriteConfiguration configuration) {
        this.configuration = configuration;
    }
}

