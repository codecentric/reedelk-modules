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
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Load static page")
@Component(service = StaticPage.class, scope = PROTOTYPE)
public class StaticPage implements ProcessorSync {

    private static final String WEBAPP_ROOT_DIR = "/webapp/";
    private static final String INDEX_PAGE = "index.html";
    private static final String PATH_PARAM_PAGE = "page";
    private static final String MESSAGE_ATTRIBUTE_PATH_PARAMS = "pathParams";

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        MessageAttributes messageAttributes = message.attributes();

        Map<String, String> pathParams = messageAttributes.get(MESSAGE_ATTRIBUTE_PATH_PARAMS);

        String theRequestedPage = pathParams.get(PATH_PARAM_PAGE);

        if (isRoot(theRequestedPage)) {
            theRequestedPage = INDEX_PAGE;
        }

        String pageFileExtension = FileUtils.getExtension(theRequestedPage);

        MimeType mimeType = MimeType.fromFileExtension(pageFileExtension);

        String requestedFile = WEBAPP_ROOT_DIR + theRequestedPage;

        InputStream input = getClass().getResourceAsStream(requestedFile);

        byte[] data;
        try {
            data = ByteArrayUtils.readFrom(input);
        } catch (IOException e) {
            throw new ESBException(e);
        }

        TypedContent<byte[]> content = new ByteArrayContent(data, mimeType);

        return MessageBuilder.get().typedContent(content).build();
    }

    private static boolean isRoot(String path) {
        return path == null || "/".equals(path);
    }
}
