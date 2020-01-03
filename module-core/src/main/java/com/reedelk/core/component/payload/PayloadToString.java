package com.reedelk.core.component.payload;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.MimeTypeCombo;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.message.content.factory.TypedContentFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Payload To String")
@Component(service = PayloadToString.class, scope = PROTOTYPE)
public class PayloadToString implements ProcessorSync {

    @Reference
    private ConverterService converterService;

    @MimeTypeCombo
    @Property("Mime type")
    @Default(MimeType.MIME_TYPE_TEXT_PLAIN)
    private String mimeType;

    private MimeType wantedMimeType;

    @Override
    public void initialize() {
        requireNotBlank(mimeType, "MimeType must not be empty");
        this.wantedMimeType = MimeType.parse(mimeType);
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        Object payload = message.payload();

        String converted = converterService.convert(payload, String.class);

        TypedContent<?> typedContent = TypedContentFactory.from(converted, wantedMimeType);

        return MessageBuilder.get()
                .typedContent(typedContent)
                .attributes(message.attributes())
                .build();
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
