package com.esb.foonnel.core.component;

import com.esb.foonnel.api.component.Join;
import com.esb.foonnel.api.message.MemoryTypedContent;
import com.esb.foonnel.api.message.Message;
import com.esb.foonnel.api.message.MimeType;
import com.esb.foonnel.api.message.Type;
import org.osgi.service.component.annotations.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = JoinPayload.class, scope = PROTOTYPE)
public class JoinPayload implements Join {

    private String delimeter;

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
                .collect(Collectors.joining(delimeter));

        Message message = new Message();
        Type type = new Type(MimeType.TEXT, String.class);
        MemoryTypedContent<String> typedContent = new MemoryTypedContent<>(combinedPayload, type);
        message.setTypedContent(typedContent);
        return message;
    }

    public void setDelimeter(String delimeter) {
        this.delimeter = delimeter;
    }

}
