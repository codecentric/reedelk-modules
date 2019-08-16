package com.reedelk.rest.component;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Required;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.ByteArrayStreamType;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("REST Client")
@Component(service = RestCall.class, scope = PROTOTYPE)
public class RestCall implements ProcessorSync {

    @Property("Request url")
    @Default("localhost")
    @Required
    private String requestUrl;

    @Property("Method")
    @Default("GET")
    @Required
    private RestMethod method;


    HttpClient.ResponseReceiver<?> receiver;

    private HttpClient.ResponseReceiver<?> getReceiver() {
        if (receiver == null) {
            synchronized (this) {
                if (receiver == null) {
                    HttpClient client = HttpClient.create();
                    receiver = client.get().uri(requestUrl);
                }
            }
        }
        return receiver;
    }
    @Override
    public Message apply(Message input) {

        HttpClient.ResponseReceiver<?> receiver = getReceiver();


        MessageBuilder messageBuilder = MessageBuilder.get();

        Flux<byte[]> bytes = receiver.response((response, byteBufFlux) -> {
            messageBuilder.mimeType(MimeType.APPLICATION_JAVA);
            return byteBufFlux.asByteArray();
        });

        TypedContent content = new ByteArrayStreamType(bytes, new Type(MimeType.APPLICATION_JSON));
        messageBuilder.typedContent(content);
        return messageBuilder.build();
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }
}
