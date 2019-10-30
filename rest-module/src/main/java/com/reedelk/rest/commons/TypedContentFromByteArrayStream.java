package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.JavaType;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.StringContent;
import com.reedelk.runtime.api.message.content.TypedContent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.nio.charset.Charset;
import java.util.Optional;

public class TypedContentFromByteArrayStream {

    private TypedContentFromByteArrayStream() {
    }

    public static TypedContent<?> fromMimeType(Publisher<byte[]> byteArrayStream, MimeType mimeType) {

        if (String.class == JavaType.from(mimeType)) {

            // If it  is a String, then we check if the charset is present
            // in the mime type to be used for the string conversion.
            Optional<Charset> charset = mimeType.getCharset();

            // Map each byte array of the stream to a string
            Flux<String> streamAsString = Flux.from(byteArrayStream).map(bytes -> {
                Charset conversionCharset = charset.orElseGet(Charset::defaultCharset);
                return new String(bytes, conversionCharset);
            });

            // The TypedContent is String stream.
            return new StringContent(streamAsString, mimeType);

        } else {

            return new ByteArrayContent(byteArrayStream, mimeType);

        }
    }
}
