package com.reedelk.rest.server;

import com.reedelk.rest.configuration.listener.ErrorResponse;
import com.reedelk.rest.configuration.listener.Response;
import com.reedelk.rest.server.mapper.HttpRequestMessageMapper;
import com.reedelk.rest.server.mapper.MessageHttpResponseMapper;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;

public class HttpRequestHandler implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {

    private InboundEventListener inboundEventListener;
    private MessageHttpResponseMapper responseMapper;
    private HttpRequestMessageMapper requestMapper;

    private HttpRequestHandler() {
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
        Message inMessage = requestMapper.map(request);
        // 2. Pass down through the processors pipeline the Message
        return Mono.just(inMessage)
                // 3. Maps back the out Message to the HTTP response
                .flatMap(message -> Mono.create((Consumer<MonoSink<Publisher<byte[]>>>) sink ->
                        inboundEventListener.onEvent(message, new OnPipelineResult(sink, response))))
                // 4. Streams back to the HTTP response channel the response data stream
                .flatMap(byteStream -> Mono.from(response.sendByteArray(byteStream)));
    }

    private class OnPipelineResult implements OnResult {

        private final MonoSink<Publisher<byte[]>> sink;
        private final HttpServerResponse response;

        private OnPipelineResult(MonoSink<Publisher<byte[]>> sink, HttpServerResponse response) {
            this.sink = sink;
            this.response = response;
        }

        @Override
        public void onResult(Message outMessage, FlowContext flowContext) {
            try {
                Publisher<byte[]> payload =
                        responseMapper.map(outMessage, response, flowContext);
                sink.success(payload);

            } catch (Exception exception) {
                Publisher<byte[]> payload =
                        responseMapper.map(exception, response, flowContext);
                sink.success(payload);
            }
        }

        @Override
        public void onError(Throwable exception, FlowContext flowContext) {
            if (exception instanceof RejectedExecutionException) {
                // Server is too  busy, there are not enough Threads able to handle the request.
                response.status(SERVICE_UNAVAILABLE);
                String responseMessage = SERVICE_UNAVAILABLE.code() + " Service Temporarily Unavailable (Server is too busy)";
                sink.success(Mono.just(responseMessage.getBytes()));

            } else {
                Publisher<byte[]> payload =
                        responseMapper.map(exception, response, flowContext);
                sink.success(payload);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String matchingPath;

        private Response response;
        private ErrorResponse errorResponse;
        private ScriptEngineService scriptEngine;
        private InboundEventListener inboundEventListener;

        public Builder matchingPath(String matchingPath) {
            this.matchingPath = matchingPath;
            return this;
        }

        public Builder response(Response response) {
            this.response = response;
            return this;
        }

        public Builder errorResponse(ErrorResponse errorResponse) {
            this.errorResponse = errorResponse;
            return this;
        }

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder inboundEventListener(InboundEventListener inboundEventListener) {
            this.inboundEventListener = inboundEventListener;
            return this;
        }

        public HttpRequestHandler build() {
            HttpRequestHandler handler = new HttpRequestHandler();
            handler.inboundEventListener = inboundEventListener;
            handler.requestMapper = new HttpRequestMessageMapper(matchingPath);
            handler.responseMapper = new MessageHttpResponseMapper(
                    scriptEngine,
                    response,
                    errorResponse);
            return handler;
        }
    }
}
