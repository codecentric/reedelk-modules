package com.reedelk.rest.server;

import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
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
        Message inMessage = HttpRequestToMessage.from(request);
        return Mono.just(inMessage)
                .flatMap(mapProcessingPipeline()) // this one process the input message through the integration flow
                .flatMap(sendResponse(response)) // sends the response back to the Http response channel
                .doOnError(throwable -> {
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

    /**
     * Sends the given message to the Http Response channel.
     */
    private Function<Message, Mono<Void>> sendResponse(final HttpServerResponse response) {
        return message -> {
            MessageToHttpResponse.from(message, response);
            Publisher<byte[]> publisher = message.getTypedContent().asByteArrayStream();
            return Mono.from(response.sendByteArray(publisher));
        };
    }
}
