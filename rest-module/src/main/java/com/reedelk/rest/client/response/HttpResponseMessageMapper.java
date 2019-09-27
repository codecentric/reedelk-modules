package com.reedelk.rest.client.response;


import com.reedelk.rest.commons.HttpHeadersAsMap;
import com.reedelk.rest.commons.MimeTypeExtract;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.ByteArrayContent;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import reactor.core.publisher.Flux;

import static com.reedelk.rest.client.response.HttpResponseAttribute.*;

public class HttpResponseMessageMapper {

    public static Message map(HttpResponse response, Flux<byte[]> bytesStream) {
        StatusLine statusLine = response.getStatusLine();

        HttpResponseAttributes responseAttributes = new HttpResponseAttributes();
        responseAttributes.put(headers(), HttpHeadersAsMap.of(response.getAllHeaders()));
        responseAttributes.put(statusCode(), statusLine.getStatusCode());
        responseAttributes.put(reasonPhrase(), statusLine.getReasonPhrase());

        Message message = MessageBuilder.get()
                .attributes(responseAttributes)
                .build();

        MimeType mimeType = MimeTypeExtract.from(response.getAllHeaders());
        Type payloadType = new Type(mimeType);
        ByteArrayContent content = new ByteArrayContent(bytesStream, payloadType);
        message.setContent(content);
        return message;
    }
}
