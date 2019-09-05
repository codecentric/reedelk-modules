package com.reedelk.rest.server;

import com.reedelk.rest.configuration.RestListenerErrorResponse;
import com.reedelk.rest.configuration.RestListenerResponse;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.StringContent;
import com.reedelk.runtime.api.message.type.TypedContent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;

public class HttpRequestHandler implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {

    private final InboundEventListener listener;
    private final RestListenerResponse listenerResponse;
    private final RestListenerErrorResponse listenerErrorResponse;

    HttpRequestHandler(RestListenerResponse listenerResponse,
                       RestListenerErrorResponse listenerErrorResponse,
                       InboundEventListener listener) {
        this.listener = listener;
        this.listenerResponse = listenerResponse;
        this.listenerErrorResponse = listenerErrorResponse;
    }

    /**
     * This handler performs the following operations:
     * 1. Maps the incoming HTTP request to a Message
     * 2. Passes down through the processors pipeline the Message
     * 3. Maps back the out Message to the HTTP response
     * 4. Streams back to the HTTP response channel the response data stream
     */
    @Override
    public Publisher<Void> apply(HttpServerRequest request, HttpServerResponse response) {
        // 1. Map the incoming HTTP request to a Message
        Message inMessage = HttpRequestToMessage.from(new HttpRequestWrapper(request));

        return Mono.just(inMessage)

                // 2. Pass down through the processors pipeline the Message
                // 3. Maps back the out Message to the HTTP response
                .flatMap(message -> Mono.create((Consumer<MonoSink<TypedContent<?>>>) sink ->
                        listener.onEvent(message, new OnPipelineResult(sink, response))))

                // 4. Streams back to the HTTP response channel the response data stream
                .flatMap(typedContent -> Mono.from(response.sendByteArray(typedContent.asByteArrayStream())));
    }

    private class OnPipelineResult implements OnResult {

        private final MonoSink<TypedContent<?>> sink;
        private final HttpServerResponse response;

        private OnPipelineResult(MonoSink<TypedContent<?>> sink, HttpServerResponse response) {
            this.sink = sink;
            this.response =  response;
        }

        @Override
        public void onResult(Message outMessage, Context context) {
            // Handle status
            // Handle content type
            // Handle additional headers
            MessageToHttpResponse.from(outMessage, context, response, listenerResponse);

            // Handle payload (keep in consideration listener response - in case of custom payload - )
            sink.success(outMessage.getTypedContent());
        }

        @Override
        public void onError(Throwable exception, Context context) {
            if (exception instanceof RejectedExecutionException) {
                // Server is too  busy, there are not enough Threads able to handle the request.
                response.status(SERVICE_UNAVAILABLE);
                String responseMessage = SERVICE_UNAVAILABLE.code() + " Service Temporarily Unavailable (Server is too busy)";
                sink.success(new StringContent(responseMessage, null));

            } else {
                // Handle status
                // Handle content type
                // Handle additional headers
                MessageToHttpResponse.from(exception, context, response, listenerErrorResponse);
                response.status(INTERNAL_SERVER_ERROR);

                // Handle payload (keep in consideration listener response - in case of custom payload - )
                sink.success(new StringContent(exception.getMessage(), null));
            }
        }
    }
}
