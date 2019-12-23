package com.reedelk.core.component.join;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.MimeTypeCombo;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Join;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.message.content.factory.TypedContentFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.stream.Collectors;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Join Delimiter")
@Component(service = JoinWithDelimiter.class, scope = PROTOTYPE)
public class JoinWithDelimiter implements Join {

    @Reference
    private ConverterService converterService;

    @Property("Mime type")
    @Default(MimeType.MIME_TYPE_TEXT_PLAIN)
    @MimeTypeCombo
    private String mimeType;

    @Property("Delimiter")
    @Default(",")
    private String delimiter;

    @Override
    public Message apply(List<Message> messagesToJoin, FlowContext flowContext) {

        // Join with delimiter supports joins of only string data types.
        String combinedPayload = messagesToJoin.stream()
                .map(message -> {
                    Object messageData = message.content().data();
                    return converterService.convert(messageData, String.class);
                })
                .collect(Collectors.joining(delimiter));

        MimeType mimeType = MimeType.parse(this.mimeType);

        TypedContent<?> typedContent = TypedContentFactory.from(combinedPayload, mimeType);

        return MessageBuilder.get().typedContent(typedContent).build();
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
