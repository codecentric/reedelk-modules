package com.esb.system.component.filesystem;

import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Required;
import com.esb.api.component.Processor;
import com.esb.api.message.*;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("File Read")
@Component(service = FileReadComponent.class, scope = PROTOTYPE)
public class FileReadComponent implements Processor {

    @Property("File path")
    @Required
    private String filePath;

    @Override
    public Message apply(Message input) {

        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String fileContent = contentBuilder.toString();

        Type contentType = new Type(MimeType.APPLICATION_JSON, String.class);

        TypedContent<String> newContent = new MemoryTypedContent<>(fileContent, contentType);

        Message output = new Message();

        output.setTypedContent(newContent);

        return output;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
