package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.StringContent;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.system.api.ModuleService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Load static page")
@Component(service = StaticPage.class, scope = PROTOTYPE)
public class StaticPage implements ProcessorSync {

    @Reference
    private ModuleService moduleService;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        MessageAttributes messageAttributes = message.attributes();

        Map<String,String> pathParams = (Map<String, String>) messageAttributes.get("pathParams");

        String theRequestedPage = pathParams.get("page");

        if (theRequestedPage == null || "/".equals(theRequestedPage)) {
            theRequestedPage = "index.html";
        }

        MimeType resultMimeType;
        if (theRequestedPage.endsWith(".css")) {
            resultMimeType = MimeType.CSS;
        } else if (theRequestedPage.endsWith(".js")) {
            resultMimeType = MimeType.JAVASCRIPT;
        } else if (theRequestedPage.endsWith(".html") || theRequestedPage.endsWith(".htm")){
            resultMimeType = MimeType.HTML;
        } else {
            resultMimeType = MimeType.UNKNOWN;
        }

        String file = "/assets/" + theRequestedPage;

        InputStream input = this.getClass().getResourceAsStream(file);

        try {
            String data = readFromInputStream(input);
            TypedContent<String> content =new StringContent(data, resultMimeType);
            return MessageBuilder.get().typedContent(content).build();
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
