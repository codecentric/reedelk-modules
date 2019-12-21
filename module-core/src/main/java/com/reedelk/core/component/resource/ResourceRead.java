package com.reedelk.core.component.resource;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.FileUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.*;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import com.reedelk.runtime.api.resource.ResourceFile;
import com.reedelk.runtime.api.resource.ResourceNotFound;
import com.reedelk.runtime.api.resource.ResourceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;

import java.util.Optional;

import static com.reedelk.core.component.resource.ResourceReadConfiguration.DEFAULT_READ_BUFFER_SIZE;
import static com.reedelk.runtime.api.commons.ImmutableMap.of;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Read Resource File")
@Component(service = ResourceRead.class, scope = PROTOTYPE)
public class ResourceRead implements ProcessorSync {

    private final String attributeResourcePath =  "resourcePath";
    private final String attributeTimestamp = "timestamp";

    @Reference
    private ResourceService resourceService;

    @Property("Resource file")
    private ResourceDynamic resourceFile;

    @Property("Auto mime type")
    @Default("true")
    private boolean autoMimeType;

    @Property("Mime type")
    @MimeTypeCombo
    @Default(MimeType.MIME_TYPE_TEXT_PLAIN)
    @When(propertyName = "autoMimeType", propertyValue = "false")
    @When(propertyName = "autoMimeType", propertyValue = When.BLANK)
    private String mimeType;

    @Property("Configuration")
    private ResourceReadConfiguration configuration;

    private int readBufferSize;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        try {

            ResourceFile<byte[]> resourceFile = resourceService.find(this.resourceFile, readBufferSize, flowContext, message);

            String resourceFilePath = resourceFile.path();

            MimeType actualMimeType = mimeTypeFrom(autoMimeType, mimeType, resourceFilePath);

            Publisher<byte[]> dataStream = resourceFile.data();

            TypedContent<byte[]> content = new ByteArrayContent(dataStream, actualMimeType);

            MessageAttributes attributes =
                    new DefaultMessageAttributes(ResourceRead.class,
                            of(attributeResourcePath, resourceFilePath,
                                    attributeTimestamp, System.currentTimeMillis()));

            return MessageBuilder.get().attributes(attributes).typedContent(content).build();

        } catch (ResourceNotFound resourceNotFound) {
            throw new ESBException(resourceNotFound);
        }
    }

    @Override
    public void initialize() {
        readBufferSize =
                Optional.ofNullable(configuration)
                .flatMap(resourceReadConfiguration ->
                        Optional.ofNullable(resourceReadConfiguration.getReadBufferSize()))
                        .orElse(DEFAULT_READ_BUFFER_SIZE);
    }

    public void setResourceFile(ResourceDynamic resourceFile) {
        this.resourceFile = resourceFile;
    }

    public void setAutoMimeType(boolean autoMimeType) {
        this.autoMimeType = autoMimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setConfiguration(ResourceReadConfiguration configuration) {
        this.configuration = configuration;
    }

    private static MimeType mimeTypeFrom(boolean autoMimeType, String mimeType, String filePath) {
        if (autoMimeType) {
            String pageFileExtension = FileUtils.getExtension(filePath);
            return MimeType.fromFileExtension(pageFileExtension);
        } else {
            return MimeType.parse(mimeType);
        }
    }
}
