package com.reedelk.rest.client.body;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;

import static java.lang.Boolean.TRUE;

public class BodyEvaluator {

    private static final BodyProvider EMPTY_BODY_METHOD_PROVIDER = new EmptyBodyMethodProvider();

    private ScriptEngineService scriptEngine;
    private RestMethod method;
    private boolean chunked;
    private String body;

    private BodyEvaluator() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public BodyProvider provider(Message message, FlowContext flowContext) {
        if (!method.hasBody()) {
            return EMPTY_BODY_METHOD_PROVIDER;
        }
        return chunked ?
                new StreamBodyProvider(BodyStreamProvider.from(message, flowContext, body, scriptEngine)) :
                new ByteArrayBodyProvider(new byte[0]);
    }

    public static class Builder {

        private ScriptEngineService scriptEngine;
        private RestMethod method;
        private Boolean chunked;
        private String body;

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder chunked(Boolean chunked) {
            this.chunked = chunked;
            return this;
        }

        public Builder method(RestMethod method) {
            this.method = method;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public BodyEvaluator build() {
            BodyEvaluator evaluator = new BodyEvaluator();
            evaluator.chunked = TRUE.equals(chunked);
            evaluator.scriptEngine = scriptEngine;
            evaluator.method = method;
            evaluator.body = body;
            return evaluator;
        }
    }

    private static class EmptyBodyMethodProvider implements BodyProvider {
    }

    private static class StreamBodyProvider implements BodyProvider {

        private final Publisher<byte[]> stream;

        StreamBodyProvider(Publisher<byte[]> stream) {
            this.stream = stream;
        }

        @Override
        public Publisher<byte[]> asStream() {
            return stream;
        }
    }

    private static class ByteArrayBodyProvider implements BodyProvider {

        private final byte[] bytes;

        ByteArrayBodyProvider(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public byte[] asByteArray() {
            return bytes;
        }
    }
}
