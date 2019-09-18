package com.reedelk.rest.apacheclient;


import com.reedelk.rest.commons.IsSuccessfulStatus;
import com.reedelk.rest.commons.MimeTypeExtract;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.ByteArrayStreamContent;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import reactor.core.publisher.Flux;

import static com.reedelk.rest.apacheclient.HttpResponseAttribute.*;

public class HttpResponseMessageMapper {

    public static Message map(HttpResponse response, Flux<byte[]> bytesStream) {
        StatusLine statusLine = response.getStatusLine();

        HttpResponseAttributes responseAttributes = new HttpResponseAttributes();
        responseAttributes.put(headers(), response.getAllHeaders());
        responseAttributes.put(statusCode(), statusLine.getStatusCode());
        responseAttributes.put(reasonPhrase(), statusLine.getReasonPhrase());

        Message message = MessageBuilder.get()
                .attributes(responseAttributes)
                .build();

        MimeType mimeType = MimeTypeExtract.from(response.getAllHeaders());
        Type payloadType = new Type(mimeType);
        ByteArrayStreamContent content = new ByteArrayStreamContent(bytesStream, payloadType);
        message.setContent(content);

        if (IsSuccessfulStatus.status(statusLine.getStatusCode())) {
            return message;
        } else {
            // If the response is not in the Range 2xx, we throw an exception.
            // TODO: The not successful should be parameterized from the client config.
            // It should be like: If the esb exception contains a Message
            // or  if the exception type is a message, then the return
            // should be the content of the message. Otherwise if it is
            // a normal exception it should contain the status code.
            throw new ESBException(message);
        }
    }
}
