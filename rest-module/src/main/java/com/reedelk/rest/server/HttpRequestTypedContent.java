package com.reedelk.rest.server;

import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

// TODO: THE TO STRING SHOULD RETURN the actual toString value
// if and only if the Flux has been resolved already with getAsBytes, otherwise not!!!!!!!!
public class HttpRequestTypedContent implements TypedContent<Flux<byte[]>> {

    private final Type type;
    private final Flux<byte[]> byteArrayStream;
    private byte[] consumedContent;

    public HttpRequestTypedContent(Flux<byte[]> byteArrayStream, MimeType mimeType) {
        this.byteArrayStream = byteArrayStream;
        this.type = new Type(mimeType);
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public Flux<byte[]> content() {
        return byteArrayStream;
    }

    @Override
    public String asString() {
        return asString(Charset.defaultCharset());
    }

    @Override
    public String asString(Charset charset) {
        return new String(asByteArray(), charset);
    }

    @Override
    public byte[] asByteArray() {
        return getAsBytes();
    }

    @Override
    public Publisher<String> asStringStream() {
        return asStringStream(Charset.defaultCharset());
    }

    @Override
    public Publisher<String> asStringStream(Charset charset) {
        return byteArrayStream.map(bytes -> new String(bytes, charset));
    }

    @Override
    public Publisher<byte[]> asByteArrayStream() {
        return byteArrayStream;
    }

    @Override
    public String toString() {
        return "HttpRequestTypedContent{" +
                "type=" + type +
                ", payload=" + asString() +
                '}';
    }

    // We can only consume it once...
    private byte[] getAsBytes() {
        if (consumedContent == null) {
            synchronized (this) {
                if (consumedContent == null) {
                    List<byte[]> block = byteArrayStream.collectList().block();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    block.forEach(bytes -> {
                        try {
                            baos.write(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    try {
                        baos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    consumedContent = baos.toByteArray();
                }
            }
        }
        return consumedContent;
    }
}
