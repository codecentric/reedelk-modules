package com.esb.rest.server;

import com.esb.api.component.InboundEventListener;
import com.esb.api.component.OnResult;
import com.esb.api.message.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

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
                .flatMap(mapProcessingPipeline())
                .flatMap(outMessage -> {
                    // TODO : On error
                    Object payload = outMessage.getTypedContent().getContent();
                    NettyOutbound nettyOutbound = response.sendString(Mono.just((String) payload));
                    return Mono.from(nettyOutbound);
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
