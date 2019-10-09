package com.reedelk.rest.server;

import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Optional;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_LENGTH;

public class BodyProviderStreamNone implements BodyProvider {

    private static final String ZERO_CONTENT_LENGTH = "0";

    private final ScriptEngineService scriptEngine;
    private final DynamicByteArray responseBody;
    private final DynamicByteArray errorResponseBody;

    BodyProviderStreamNone(ScriptEngineService scriptEngine, DynamicByteArray responseBody, DynamicByteArray errorResponseBody) {
        this.responseBody = responseBody;
        this.scriptEngine = scriptEngine;
        this.errorResponseBody = errorResponseBody;
    }

    // No streaming, single valued Stream (Mono)
    @Override
    public Publisher<byte[]> from(HttpServerResponse response, Message message, FlowContext flowContext) {
        Optional<byte[]> evaluated = scriptEngine.evaluate(responseBody, message, flowContext);
        return publisherFrom(response, evaluated);
    }

    // No streaming, single valued Stream (Mono)
    @Override
    public Publisher<byte[]> from(HttpServerResponse response, Throwable throwable, FlowContext flowContext) {
        try {
            Optional<byte[]> evaluated = scriptEngine.evaluate(errorResponseBody, throwable, flowContext);
            return publisherFrom(response, evaluated);
        } catch (Exception exception) {
            // Evaluating an error response, cannot throw again an exception,
            // Therefore we catch any exception and we return the exception message.
            byte[] exceptionBytes = StackTraceUtils.asByteArray(exception);
            response.addHeader(CONTENT_LENGTH, String.valueOf(exceptionBytes.length));
            return Mono.just(exceptionBytes);
        }
    }

    private static Publisher<byte[]> publisherFrom(HttpServerResponse response, Optional<byte[]> evaluated) {
        if (evaluated.isPresent()) {
            byte[] responseBody = evaluated.get();
            response.addHeader(CONTENT_LENGTH, String.valueOf(responseBody.length));
            return Mono.just(responseBody);
        } else {
            response.addHeader(CONTENT_LENGTH, ZERO_CONTENT_LENGTH);
            return Mono.empty();
        }
    }
}
