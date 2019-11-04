package com.reedelk.rest.client.response;


import com.reedelk.rest.commons.HttpHeadersAsMap;
import com.reedelk.rest.commons.MimeTypeExtract;
import com.reedelk.rest.commons.TypedContentFromByteArrayStream;
import com.reedelk.runtime.api.message.DefaultMessageAttributes;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseMessageMapper {

    public static Message map(HttpResponse response, Flux<byte[]> bytesStream) {
        StatusLine statusLine = response.getStatusLine();

        Map<String, Serializable> attributes = new HashMap<>();
        attributes.put(HttpResponseAttribute.STATUS_CODE, statusLine.getStatusCode());
        attributes.put(HttpResponseAttribute.REASON_PHRASE, statusLine.getReasonPhrase());
        attributes.put(HttpResponseAttribute.HEADERS, HttpHeadersAsMap.of(response.getAllHeaders()));

        DefaultMessageAttributes responseAttributes = new DefaultMessageAttributes(attributes);

        Message message = MessageBuilder.get()
                .attributes(responseAttributes)
                .build();

        MimeType mimeType = MimeTypeExtract.from(response.getAllHeaders());

        TypedContent<?> content =
                TypedContentFromByteArrayStream.fromMimeType(bytesStream, mimeType);

        message.setContent(content);
        return message;
    }
}
