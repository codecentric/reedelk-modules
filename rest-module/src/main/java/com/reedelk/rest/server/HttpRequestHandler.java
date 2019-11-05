package com.reedelk.rest.server;

import com.reedelk.rest.ServerTooBusyException;
import com.reedelk.rest.configuration.StreamingMode;
import com.reedelk.rest.configuration.listener.ErrorResponse;
import com.reedelk.rest.configuration.listener.Response;
import com.reedelk.rest.server.body.BodyProvider;
import com.reedelk.rest.server.body.BodyProviderStreamAlways;
import com.reedelk.rest.server.body.BodyProviderStreamAuto;
import com.reedelk.rest.server.body.BodyProviderStreamNone;
import com.reedelk.rest.server.mapper.HttpRequestMessageMapper;
import com.reedelk.rest.server.mapper.MessageHttpResponseMapper;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_LENGTH;
import static com.reedelk.runtime.api.commons.StackTraceUtils.asByteArray;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static java.lang.String.format;

public class HttpRequestHandler implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {

    private InboundEventListener inboundEventListener;
    private MessageHttpResponseMapper responseMapper;
    private HttpRequestMessageMapper requestMapper;
    private BodyProvider bodyProvider;

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

        Message inputMessage;
        try {
            // Map HTTP request to Message object.
            inputMessage = requestMapper.map(request);

        } catch (Exception exception) {

            byte[] bodyBytes = asByteArray(exception);

            response.status(INTERNAL_SERVER_ERROR);

            response.addHeader(CONTENT_LENGTH, String.valueOf(bodyBytes.length));

            return Mono.from(response.sendByteArray(Mono.just(bodyBytes)));
        }

        // Propagate the event down to the processors chain by sending an event.
        return Mono.just(inputMessage)

                // Propagate and map back the out Message as HTTP response.
                // The sink is used to stream out the bytes to be sent to the client.
                .flatMap(message -> Mono.create((Consumer<MonoSink<Publisher<byte[]>>>) bytesSink ->
                        inboundEventListener.onEvent(message, new OnPipelineResult(bytesSink, response))))

                // Streams back to the HTTP response channel the response data stream
                .flatMap(byteStream -> Mono.from(response.sendByteArray(byteStream)));
    }

    /**
     * This class maps back the out Message object to the http response to be sent.
     * It feeds a sink which will be used to stream ta back to the client.
     */
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

                responseMapper.map(outMessage, response, flowContext);

                Publisher<byte[]> body = bodyProvider.from(response, outMessage, flowContext);

                sink.success(body);

            } catch (Exception exception) {

                responseMapper.map(exception, response, flowContext);

                Publisher<byte[]> body = bodyProvider.from(response, exception, flowContext);

                sink.success(body);
            }
        }

        @Override
        public void onError(Throwable exception, FlowContext flowContext) {

            Throwable realException = exception;

            if (exception instanceof RejectedExecutionException) {
                // Server is too  busy, there are not enough Threads able to handle the request.

                String errorMessage = SERVICE_UNAVAILABLE.code() + " Service Temporarily Unavailable (Server is too busy)";

                realException = new ServerTooBusyException(errorMessage, exception);
            }

            responseMapper.map(realException, response, flowContext);

            Publisher<byte[]> body = bodyProvider.from(response, realException, flowContext);

            sink.success(body);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String matchingPath;

        private Response response;
        private StreamingMode streaming;
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

        public Builder streaming(StreamingMode streaming) {
            this.streaming = streaming;
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
            handler.responseMapper = new MessageHttpResponseMapper(scriptEngine, response, errorResponse);
            handler.bodyProvider = createBodyProvider();
            return handler;
        }

        private BodyProvider createBodyProvider() {
            DynamicByteArray bodyResponse = response == null ? null : response.getBody();
            DynamicByteArray bodyErrorResponse = errorResponse == null ? null : errorResponse.getBody();
            if (StreamingMode.NONE.equals(streaming)) {
                return new BodyProviderStreamNone(scriptEngine, bodyResponse, bodyErrorResponse);
            } else if (StreamingMode.ALWAYS.equals(streaming)) {
                return new BodyProviderStreamAlways(scriptEngine, bodyResponse, bodyErrorResponse);
            } else if (StreamingMode.AUTO.equals(streaming)) {
                return new BodyProviderStreamAuto(scriptEngine, bodyResponse, bodyErrorResponse);
            } else {
                throw new IllegalArgumentException(format("Execution strategy not available for streaming mode '%s'", streaming));
            }
        }
    }
}
