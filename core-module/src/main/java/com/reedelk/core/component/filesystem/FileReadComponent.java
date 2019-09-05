package com.reedelk.core.component.filesystem;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("File Read")
@Component(service = FileReadComponent.class, scope = PROTOTYPE)
public class FileReadComponent implements ProcessorSync {

    @Property("File path")
    private String filePath;

    @Override
    public Message apply(Message input, Context context) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO: we should add a property to specify the mime type of the content...
        String fileContent = contentBuilder.toString();

        return MessageBuilder.get().text(fileContent).build();
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
