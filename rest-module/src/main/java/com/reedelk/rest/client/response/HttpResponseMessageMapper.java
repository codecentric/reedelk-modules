package com.reedelk.rest.client.response;


import com.reedelk.rest.commons.HttpHeadersAsMap;
import com.reedelk.rest.commons.MimeTypeExtract;
import com.reedelk.runtime.api.commons.TypedContentFromByteArrayStream;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.TypedContent;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import reactor.core.publisher.Flux;

public class HttpResponseMessageMapper {

    public static Message map(HttpResponse response, Flux<byte[]> bytesStream) {
        StatusLine statusLine = response.getStatusLine();

        HttpResponseAttributes responseAttributes = new HttpResponseAttributes();
        responseAttributes.put(HttpResponseAttribute.STATUS_CODE, statusLine.getStatusCode());
        responseAttributes.put(HttpResponseAttribute.REASON_PHRASE, statusLine.getReasonPhrase());
        responseAttributes.put(HttpResponseAttribute.HEADERS, HttpHeadersAsMap.of(response.getAllHeaders()));

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
