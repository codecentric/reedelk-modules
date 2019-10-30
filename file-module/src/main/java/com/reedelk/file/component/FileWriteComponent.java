package com.reedelk.file.component;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

@ESBComponent("File write")
@Component(service = FileWriteComponent.class, scope = ServiceScope.PROTOTYPE)
public class FileWriteComponent implements ProcessorAsync {

    @Property("Upload directory")
    private String uploadDirectory;
    @Property("Upload extension")
    private String extension;

    @Override
    public void apply(Message input, FlowContext flowContext, OnResult callback) {

        TypedPublisher<?> originalStream = input.content().stream();
        // We must convert this stream into a byte stream so that we can write it.

        Path path = Paths.get(uploadDirectory, UUID.randomUUID().toString() + "." + extension);

        TypedPublisher<byte[]> stream = null;

        Flux.from(Flux.from(stream))
                .reduceWith(() -> {
                    try {
                        return Files.newOutputStream(path, WRITE, CREATE_NEW);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }, (outputStream, s) -> {
                    try {
                        outputStream.write(s);
                        return outputStream;
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .doOnSuccessOrError((out, throwable) -> {
                    try {
                        out.close();
                        Message done = MessageBuilder.get()
                                .text("Written on path: " + path.toString())
                                .build();
                        callback.onResult(done, flowContext);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }).subscribe();
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }
}

