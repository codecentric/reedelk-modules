package com.reedelk.rest.client;


import com.reedelk.runtime.api.commons.JavaType;
import com.reedelk.runtime.api.message.type.*;

import java.nio.charset.Charset;
import java.util.Optional;

class HttpResponseContentMapper {

    static TypedContent map(HttpResponseWrapper response) {
        MimeType mimeType = response.mimeType();

        byte[] dataArray = response.data();
        if (String.class == JavaType.from(mimeType)) {
            return mapStringJavaType(dataArray, mimeType);
        } else {
            // Generic byte array.
            Type type = new Type(mimeType, byte[].class);
            return new ByteArrayContent(dataArray, type);
        }
    }

    private static TypedContent mapStringJavaType(byte[] dataArray, MimeType mimeType) {
        // If it  is a String, then we check the charset if present
        // in the mime type to be used for the string conversion.
        Optional<Charset> charset = mimeType.getCharset();

        Charset conversionCharset = charset.orElseGet(Charset::defaultCharset);
        String dataAsString = new String(dataArray, conversionCharset);

        // The TypedContent is String stream.
        return new StringContent(dataAsString, mimeType);
    }
}
