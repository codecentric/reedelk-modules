package com.reedelk.file.component;

import com.reedelk.file.write.FileWriteConfiguration;
import com.reedelk.file.write.WriteConfiguration;
import com.reedelk.file.write.Writer;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.service.ConverterService;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.reedelk.file.commons.Messages.FileWriteComponent.ERROR_CREATING_DIRECTORIES;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

@ESBComponent("File write")
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

    @Property("Configuration")
    private FileWriteConfiguration configuration;

    private final Writer writer = new Writer();

    @Override
    public void apply(Message message, FlowContext flowContext, OnResult callback) {


        Optional<String> evaluated = scriptService.evaluate(filePath, message, flowContext);

        if (evaluated.isPresent()) {

            WriteConfiguration config = new WriteConfiguration(configuration);

            String filePath = evaluated.get();

            Path finalPath = isBlank(basePath) ? Paths.get(filePath) : Paths.get(basePath, filePath);

            if (config.isCreateParentDirectory()) {
                try {
                    Files.createDirectories(finalPath.getParent());
                } catch (IOException exception) {
                    String errorMessage = ERROR_CREATING_DIRECTORIES.format(finalPath.toString(), exception.getMessage());
                    callback.onError(new ESBException(errorMessage, exception), flowContext);
                    return;
                }
            }

            TypedPublisher<?> originalStream = message.content().stream();

            TypedPublisher<byte[]> originalStreamAsBytes = converterService.convert(originalStream, byte[].class);

            // TODO: Try catch any exception hwere
            writer.writeTo(config, flowContext, callback, finalPath, originalStreamAsBytes);

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

    public void setConfiguration(FileWriteConfiguration configuration) {
        this.configuration = configuration;
    }
}

