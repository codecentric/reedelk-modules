package com.esb.component.payload;

import com.esb.api.annotation.Default;
import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Required;
import com.esb.api.component.Join;
import com.esb.api.message.MemoryTypedContent;
import com.esb.api.message.Message;
import com.esb.api.message.MimeType;
import com.esb.api.message.Type;
import org.osgi.service.component.annotations.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Join Payload")
@Component(service = JoinPayload.class, scope = PROTOTYPE)
public class JoinPayload implements Join {

    @Property("Delimiter")
    @Default(",")
    @Required
    private String delimiter;

    @Override
    public Message apply(List<Message> messagesToJoin) {
        String combinedPayload = messagesToJoin.stream()
                .map(Message::getTypedContent)
                .map(typedContent -> {
                    if (typedContent.getType().getTypeClass().isAssignableFrom(String.class)) {
                        return (String) typedContent.getContent();
                    } else {
                        return null;
                    }
                })
                .collect(Collectors.joining(delimiter));

        Message message = new Message();
        Type type = new Type(MimeType.TEXT, String.class);
        MemoryTypedContent<String> typedContent = new MemoryTypedContent<>(combinedPayload, type);
        message.setTypedContent(typedContent);
        return message;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

}
