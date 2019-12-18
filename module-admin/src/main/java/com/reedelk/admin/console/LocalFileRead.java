package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Hidden;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.commons.ModuleId;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.file.ModuleFileProvider;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.commons.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;

import java.io.FileNotFoundException;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Local file read")
@Component(service = LocalFileRead.class, scope = PROTOTYPE)
public class LocalFileRead implements ProcessorSync {

    private static final int READ_LOCAL_FILE_BUFFER_SIZE = 65536;

    private final String webappFolder = "/webapp/";
    private final String indexPage = "index.html";
    private final String pathParamPage = "page";
    private final String pathParamAttribute = "pathParams";

    @Hidden
    @Property("Module Id")
    private ModuleId moduleId;

    @Reference
    private ModuleFileProvider moduleFileProvider;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        Map<String, String> pathParams = message.getAttributes().get(pathParamAttribute);

        String requestedFile = pathParams.getOrDefault(pathParamPage, indexPage);

        String finalFilePath = webappFolder + requestedFile;

        String pageFileExtension = FileUtils.getExtension(finalFilePath);

        MimeType actualMimeType = MimeType.fromFileExtension(pageFileExtension);

        try {
            Publisher<byte[]> contentAsStream = moduleFileProvider.findBy(moduleId, finalFilePath, READ_LOCAL_FILE_BUFFER_SIZE);

            TypedContent<byte[]> content = new ByteArrayContent(contentAsStream, actualMimeType);

            return MessageBuilder.get().typedContent(content).build();

        } catch (FileNotFoundException e) {
            throw new ESBException(e);
        }
    }

    public void setModuleId(ModuleId moduleId) {
        this.moduleId = moduleId;
    }
}
