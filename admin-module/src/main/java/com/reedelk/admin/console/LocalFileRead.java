package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
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

import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Local file read")
@Component(service = LocalFileRead.class, scope = PROTOTYPE)
public class LocalFileRead implements ProcessorSync {

    private static final int READ_LOCAL_FILE_BUFFER_SIZE = 65536;

    private static final String WEBAPP_FOLDER = "/webapp/";

    @Reference
    private ModuleFileProvider moduleFileProvider;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        Map<String, String> pathParams = message.getAttributes().get("pathParams");

        String requestedFile = "index.html";
        if (pathParams.containsKey("page")) {
            requestedFile = pathParams.get("page");
        }

        String finalFilePath = WEBAPP_FOLDER + requestedFile;

        String pageFileExtension = FileUtils.getExtension(finalFilePath);

        MimeType actualMimeType = MimeType.fromFileExtension(pageFileExtension);

        Publisher<byte[]> contentAsStream =
                moduleFileProvider.findBy(ModuleIdProvider.get(), finalFilePath, READ_LOCAL_FILE_BUFFER_SIZE);

        TypedContent<byte[]> content = new ByteArrayContent(contentAsStream, actualMimeType);

        return MessageBuilder.get().typedContent(content).build();
    }
}
