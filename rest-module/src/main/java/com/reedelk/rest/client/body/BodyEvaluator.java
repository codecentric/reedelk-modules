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
    private String body;
    private boolean chunked;

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
                new BodyProvider() {
                    @Override
                    public Publisher<byte[]> asStream() {
                        return new StreamBodyProvider(scriptEngine, body).from(message, flowContext);
                    }
                } :
                new BodyProvider() {
                    @Override
                    public byte[] asByteArray() {
                        return new ByteArrayBodyProvider(scriptEngine, body).from(message, flowContext);
                    }
                };
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
}
