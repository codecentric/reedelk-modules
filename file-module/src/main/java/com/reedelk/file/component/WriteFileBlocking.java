package com.reedelk.file.component;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

// TODO: This is just a test to show the difference
// TODO: between the blocking and non blocking version
@ESBComponent("Write file blocking")
@Component(service = WriteFileBlocking.class, scope = ServiceScope.PROTOTYPE)
public class WriteFileBlocking implements ProcessorSync {

    private static final Logger logger = LoggerFactory.getLogger(WriteFileBlocking.class);

    @Property("Upload directory")
    private String uploadDirectory;
    @Property("Upload extension")
    private String extension;

    @Override
    public Message apply(Message message) {
        Path path = Paths.get(uploadDirectory, UUID.randomUUID().toString() + "." + extension);

        byte[] bytes = message.getTypedContent().asByteArray();
        try {
            logger.warn("Writing blocking from thread: " + Thread.currentThread());
            Files.write(path, bytes,WRITE, CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return MessageBuilder.get()
                .text("Written on path: " + path.toString())
                .build();

    }

    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}