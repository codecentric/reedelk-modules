package com.reedelk.file.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.exception.ModuleFileNotFoundException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.commons.FileUtils;
import com.reedelk.runtime.commons.StreamFromURL;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.reactivestreams.Publisher;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;

@ESBComponent("File read")
@Component(service = FileReadComponent.class, scope = ServiceScope.PROTOTYPE)
public class FileReadComponent implements ProcessorSync {

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

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        Optional<String> evaluated = service.evaluate(fileName, message, flowContext);

        return evaluated.map(filePath -> {

            MimeType actualMimeType;

            if (autoMimeType) {
                String pageFileExtension = FileUtils.getExtension(filePath);
                actualMimeType = MimeType.fromFileExtension(pageFileExtension);
            } else {
                actualMimeType = MimeType.parse(mimeType);
            }

            Publisher<byte[]> contentAsStream;
            if (StringUtils.isBlank(basePath)) {

                try {
                    contentAsStream = StreamFromURL.of(Paths.get(filePath).toUri().toURL());
                } catch (MalformedURLException e) {
                    throw new ESBException(e);
                }

            } else {

                try {
                    URL finalFilePath = Paths.get(basePath, filePath).toUri().toURL();
                    contentAsStream = StreamFromURL.of(finalFilePath);
                } catch (MalformedURLException e) {
                    throw new ESBException(e);
                }
            }

            TypedContent<byte[]> content = new ByteArrayContent(contentAsStream, actualMimeType);

            return MessageBuilder.get().typedContent(content).build();

        }).orElseThrow(() -> new ModuleFileNotFoundException("Could not find file"));
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
}
