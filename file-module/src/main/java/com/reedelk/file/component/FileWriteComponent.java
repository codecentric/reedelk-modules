package com.reedelk.file.component;

import com.reedelk.file.commons.WriteMode;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.service.ConverterService;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@ESBComponent("File write")
@Component(service = FileWriteComponent.class, scope = ServiceScope.PROTOTYPE)
public class FileWriteComponent implements ProcessorAsync {

    @Reference
    private ScriptEngineService scriptService;
    @Reference
    private ConverterService converterService;

    @Property("File name")
    private DynamicString filePath;

    @Property("Write mode")
    private WriteMode mode = WriteMode.OVERWRITE;

    @Property("Create directories")
    private boolean createParentDirectory = false;


    @Override
    public void apply(Message message, FlowContext flowContext, OnResult callback) {

        Optional<String> evaluated = scriptService.evaluate(filePath, message, flowContext);
        if (!evaluated.isPresent()) {
            callback.onError(new ESBException("Could not write file"), flowContext);
        }

        String filePath = evaluated.get();

        TypedPublisher<?> originalStream = message.content().stream();

        Path path = Paths.get(filePath);

        if (createParentDirectory) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                callback.onError(new ESBException(e), flowContext);
                return;
            }
        }

        TypedPublisher<byte[]> stream = converterService.convert(originalStream, byte[].class);

        Flux.from(Flux.from(stream))
                .reduceWith(() -> {
                    try {
                        return Files.newOutputStream(path, mode.options());
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }, (outputStream, byteChunk) -> {
                    try {
                        outputStream.write(byteChunk);
                        return outputStream;
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .doOnSuccessOrError((out, throwable) -> {

                    closeSilently(out);

                    if (throwable != null) {
                        callback.onError(new ESBException(throwable), flowContext);
                    } else {
                        Message done = MessageBuilder.get()
                                .text("Written on path: " + path.toString())
                                .build();
                        callback.onResult(done, flowContext);
                    }
                }).subscribe();
    }

    public void setFilePath(DynamicString filePath) {
        this.filePath = filePath;
    }

    public void setMode(WriteMode mode) {
        this.mode = mode;
    }

    public void setCreateParentDirectory(boolean createParentDirectory) {
        this.createParentDirectory = createParentDirectory;
    }

    private void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

