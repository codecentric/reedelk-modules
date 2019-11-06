package com.reedelk.file.component;

import com.reedelk.file.commons.CloseableUtils;
import com.reedelk.file.commons.FileWriteAttribute;
import com.reedelk.file.commons.WriteMode;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.*;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.service.ConverterService;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.reedelk.file.commons.Messages.FileWriteComponent.ERROR_CREATING_DIRECTORIES;

@ESBComponent("File write")
@Component(service = FileWrite.class, scope = ServiceScope.PROTOTYPE)
public class FileWrite implements ProcessorAsync {

    @Reference
    private ScriptEngineService scriptService;
    @Reference
    private ConverterService converterService;

    @Property("File name")
    private DynamicString filePath;

    @Property("Write mode")
    private WriteMode mode = WriteMode.OVERWRITE;

    @Property("Create directories")
    private boolean createParentDirectory;


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
            } catch (IOException exception) {
                String errorMessage = ERROR_CREATING_DIRECTORIES.format(path.toString(), exception.getMessage());
                callback.onError(new ESBException(errorMessage, exception), flowContext);
                return;
            }
        }

        TypedPublisher<byte[]> stream = converterService.convert(originalStream, byte[].class);

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        Flux.from(Flux.from(stream))
                .reduceWith(() -> {
                    try {
                        return FileChannel.open(path, mode.options());
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }, (outputStream, byteChunk) -> {
                    try {

                        if (byteChunk.length > 1024) {
                            // Write it in multiple steps
                            int offset = 0;
                            int length = 1024;
                            while (offset <= byteChunk.length) {
                                byteBuffer.put(byteChunk, offset, length);
                                offset += 1024;
                                length = byteChunk.length - offset;
                            }


                        } else {
                            byteBuffer.put(byteChunk);
                        }

                        outputStream.write(byteBuffer);

                        return outputStream;

                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .doOnSuccessOrError((out, throwable) -> {

                    CloseableUtils.closeSilently(out);

                    if (throwable != null) {
                        // An exception was thrown during the process.
                        callback.onError(new ESBException(throwable), flowContext);

                    } else {

                        MessageAttributes attributes = new DefaultMessageAttributes(ImmutableMap.of(
                                FileWriteAttribute.FILE_NAME, path.toString(),
                                FileWriteAttribute.TIMESTAMP, System.currentTimeMillis()));

                        Message outMessage = MessageBuilder.get().attributes(attributes).empty().build();

                        callback.onResult(outMessage, flowContext);
                    }
                }).subscribe();
    }

    public void setMode(WriteMode mode) {
        this.mode = mode;
    }

    public void setFilePath(DynamicString filePath) {
        this.filePath = filePath;
    }

    public void setCreateParentDirectory(boolean createParentDirectory) {
        this.createParentDirectory = createParentDirectory;
    }
}

