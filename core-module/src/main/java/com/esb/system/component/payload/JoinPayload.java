package com.esb.system.component.payload;

import com.esb.api.annotation.Default;
import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Required;
import com.esb.api.component.Join;
import com.esb.api.message.Message;
import com.esb.api.message.MessageBuilder;
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
                    if (typedContent.type().getTypeClass().isAssignableFrom(String.class)) {
                        return (String) typedContent.content();
                    } else {
                        return null;
                    }
                })
                .collect(Collectors.joining(delimiter));

        return MessageBuilder.get().text(combinedPayload).build();
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

}
