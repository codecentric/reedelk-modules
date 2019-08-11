package com.esb.rest.server;

import com.esb.api.component.InboundEventListener;
import com.esb.api.component.OnResult;
import com.esb.api.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HttpRequestHandler implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {

    private final InboundEventListener listener;

    HttpRequestHandler(InboundEventListener listener) {
        this.listener = listener;
    }

    @Override
    public Publisher<Void> apply(HttpServerRequest request, HttpServerResponse response) {
        Message inMessage = MapHttpRequestToMessage.from(request);
        return Mono.just(inMessage)
                .flatMap(mapProcessingPipeline()) // this one process the input message through the integration flow
                .flatMap(outMessage -> {
                    MapMessageToHttpResponse.from(outMessage, response);
                    Object payload = outMessage.getTypedContent().getContent();

                    if (payload instanceof String) {
                        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
                        buffer.writeCharSequence((CharSequence) payload, StandardCharsets.UTF_8);
                        return Mono.from(response.sendObject(buffer));
                    } else {
                        return response.send();
                    }
                }).doOnError(throwable -> {
                    response.send(Mono.error(throwable)); // 500
                });
    }

    /**
     * The listener Invokes the integration flow processor pipeline.
     */
    private Function<Message, Mono<Message>> mapProcessingPipeline() {
        return message -> Mono.create(sink -> listener.onEvent(message, new OnResult() {
            @Override
            public void onResult(Message outMessage) {
                sink.success(outMessage);
            }

            @Override
            public void onError(Throwable throwable) {
                sink.error(throwable);
            }
        }));
    }
}
