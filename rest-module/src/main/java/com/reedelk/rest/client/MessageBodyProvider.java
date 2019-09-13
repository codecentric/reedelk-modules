package com.reedelk.rest.client;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

// TODO: This class needs work.
// TODO: The content length thingy, this is how it works:
// If the content-length is set by the user, then the POST/PUT/DELETE are NOT
// Transfer 'chunked', otherwise  they are by default chunked. Therefore,
// the behaviour is going to be: if the body is a stream, then it will be  chunked,
//otherwise (since we have the number of bytes) we will set the content length
// in the header.
public class MessageBodyProvider {

    public static BodyProvider from(Message message, String body, ScriptEngineService scriptEngine) {
        // This code is only executed if and only if the request is
        // either POST,PUT or DELETE. For all other HTTP methods this is not executed.
        return () -> {

            if (StringUtils.isBlank(body)) {
                return new EmptyBodyProvider();
            }

            // TODO: What if we need to send the content length and it is a stream?
            if (ScriptUtils.isScript(body)) {
                Object result = scriptEngine.evaluate(body, message);
                // This one should be converted as byte stream.
            }
            // TODO: Take the body and interpret it..(might be javascript)
            // Request body has to be provided if and only if it is a POST,PUT,DELETE.
            // Also if the body is null, don't bother to do anything, just
            // send empty byte array buffer.
            // If the body is already a stream, then we just stream it upstream. (we support stream outbound)
            // if the body is stream, we actually don't know the length of the body to be up-streamed.
            byte[] bodyAsBytes = message.getContent().asByteArray();
            Flux<ByteBuf> dataStream = Flux.just(Unpooled.wrappedBuffer(bodyAsBytes));
            return new DefaultBodyProvider(dataStream, bodyAsBytes.length);
        };
    }


    static class DefaultBodyProvider implements BodyProviderData {

        private final Publisher<? extends ByteBuf> data;
        private final int length;

        DefaultBodyProvider(Publisher<? extends ByteBuf> data, int length) {
            this.data = data;
            this.length = length;
        }
        @Override
        public Publisher<? extends ByteBuf> provide() {
            return data;
        }

        @Override
        public int length() {
            return length;
        }
    }

    static class EmptyBodyProvider extends DefaultBodyProvider {
        EmptyBodyProvider() {
            super(Flux.empty(), 0);
        }
    }
}
