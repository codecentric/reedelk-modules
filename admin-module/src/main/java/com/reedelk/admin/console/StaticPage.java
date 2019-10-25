package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.commons.FileUtils;
import com.reedelk.runtime.system.api.ModuleService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Load static page")
@Component(service = StaticPage.class, scope = PROTOTYPE)
public class StaticPage implements ProcessorSync {

    private static final String WEBAPP_ROOT_DIR = "/webapp/";

    @Reference
    private ModuleService moduleService;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        MessageAttributes messageAttributes = message.attributes();

        // TODO: This is wrong, should create a method <T> to get attribute and stuff...
        Map<String,String> pathParams = (Map<String, String>) messageAttributes.get("pathParams");

        String theRequestedPage = pathParams.get("page");

        if (theRequestedPage == null || "/".equals(theRequestedPage)) {
            theRequestedPage = "index.html";
        }

        String pageFileExtension = FileUtils.getExtension(theRequestedPage);
        MimeType mimeType = MimeType.fromFileExtension(pageFileExtension);

        String requestedFile = WEBAPP_ROOT_DIR + theRequestedPage;

        InputStream input = getClass().getResourceAsStream(requestedFile);
        try {
            byte[] data = readFromInputStream(input);
            TypedContent<byte[]> content = new ByteArrayContent(data, mimeType);
            return MessageBuilder.get().typedContent(content).build();
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    private byte[] readFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = inputStream.read(buffer); len != -1; len = inputStream.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
}
