package com.reedelk.rest.client;

import com.reedelk.rest.commons.IsNotSuccessful;
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

        // If the response is not in the Range 2xx, we throw an exception.
        // TODO: The not successful should be parameterized from the client config.
        if (IsNotSuccessful.status(response.status())) {
            throw new ESBException(message);
        } else {
            return message;
        }
    }
}
