package com.reedelk.rest.client;

import com.reedelk.rest.commons.IsNotSuccessful;
import com.reedelk.rest.commons.MimeTypeExtract;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.ByteArrayContent;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;

public class HttpResponseMessageMapper {

    public Message map(ClientResponseData responseData) {
        // If the response is not in the Range 2xx, we throw an exception.
        if (IsNotSuccessful.status(responseData.getStatus())) {
            // Map error
            throw new ESBException(responseData.getStatus().toString());
        } else {
            // Map success
            // We set the type of the content according to the
            // Content type header.
            MimeType mimeType = MimeTypeExtract.from(responseData.getHeaders());
            Type type = new Type(mimeType);

                    // We set the content
            TypedContent content = new ByteArrayContent(responseData.getData(), type);

            // TODO: Create attributes from response!
            HttpResponseAttributes responseAttributes = new HttpResponseAttributes();
            return MessageBuilder.get().typedContent(content)
                    .attributes(responseAttributes)
                    .build();
        }
    }
}
