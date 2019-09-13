package com.reedelk.rest.client;

import com.reedelk.rest.commons.IsSuccessful;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.TypedContent;

import static com.reedelk.rest.client.HttpResponseAttribute.*;

public class HttpResponseMessageMapper {

    public Message map(HttpResponseWrapper response) {
        HttpResponseAttributes responseAttributes = new HttpResponseAttributes();
        responseAttributes.put(headers(), response.headers());
        responseAttributes.put(statusCode(), response.status().code());
        responseAttributes.put(reasonPhrase(), response.status().reasonPhrase());

        TypedContent content = HttpResponseContentMapper.map(response);

        Message message = MessageBuilder.get()
                .attributes(responseAttributes)
                .typedContent(content)
                .build();

        if (IsSuccessful.status(response.status())) {
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
