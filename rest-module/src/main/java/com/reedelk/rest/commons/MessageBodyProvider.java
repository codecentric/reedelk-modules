package com.reedelk.rest.commons;

import com.reedelk.rest.client.BodyProvider;
import com.reedelk.rest.client.BodyProviderData;
import com.reedelk.runtime.api.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class MessageBodyProvider {

    public static BodyProvider from(Message message) {
        // This code is only executed if and only if the request is
        // either POST,PUT or DELETE. For all other HTTP methods this is not executed.
        return () -> {
            // TODO: Take the body and interpret it..(might be javascript)
            // Request body has to be provided if and only if it is a POST,PUT,DELETE.
            // Also if the body is null, don't bother to do anything, just
            // send empty byte array buffer.
            // If the body is already a stream, then we just stream it upstream. (we support stream outbound)
            byte[] bodyAsBytes = message.getContent().asByteArray();
            return new BodyProviderData() {
                @Override
                public Publisher<? extends ByteBuf> provide() {
                    return Flux.just(Unpooled.wrappedBuffer(bodyAsBytes));
                }

                @Override
                public int length() {
                    return bodyAsBytes.length;
                }
            };
        };
    }
}
