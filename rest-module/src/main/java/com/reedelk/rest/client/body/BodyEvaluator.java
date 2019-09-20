package com.reedelk.rest.client.body;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.StreamingMode;
import com.reedelk.runtime.api.service.ScriptEngineService;

import static java.lang.Boolean.TRUE;

public class BodyEvaluator {

    private static final BodyProvider EMPTY_BODY_METHOD_PROVIDER = new EmptyBodyMethodProvider();

    private BodyProvider bodyProvider;

    private BodyEvaluator() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public BodyProvider provider() {
        return bodyProvider;
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

        public Builder streaming(StreamingMode streamingMode) {
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
            if (method.hasBody()) {
                evaluator.bodyProvider = TRUE.equals(chunked) ?
                        new StreamBodyProvider(scriptEngine, body) :
                        new ByteArrayBodyProvider(scriptEngine, body);
            } else {
                evaluator.bodyProvider = EMPTY_BODY_METHOD_PROVIDER;
            }
            return evaluator;
        }
    }

    private static class EmptyBodyMethodProvider implements BodyProvider {
    }
}
