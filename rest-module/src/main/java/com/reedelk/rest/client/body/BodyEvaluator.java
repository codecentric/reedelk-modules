package com.reedelk.rest.client.body;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.StreamingMode;
import com.reedelk.runtime.api.service.ScriptEngineService;

import static com.reedelk.rest.configuration.StreamingMode.*;
import static java.lang.String.format;

public class BodyEvaluator {

    private final BodyProvider bodyProvider;

    private BodyEvaluator(BodyProvider bodyProvider) {
        this.bodyProvider = bodyProvider;
    }

    public static Builder builder() {
        return new Builder();
    }

    public BodyProvider provider() {
        return bodyProvider;
    }

    public static class Builder {

        private ScriptEngineService scriptEngine;
        private StreamingMode streaming;
        private RestMethod method;
        private String body;

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder streaming(StreamingMode streaming) {
            this.streaming = streaming;
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
            BodyProvider provider = method.hasBody() ?
                    createBodyProvider() :
                    EmptyBodyProvider.INSTANCE;
            return new BodyEvaluator(provider);
        }

        private BodyProvider createBodyProvider() {
            if (NONE.equals(streaming)) {
                return new ByteArrayBodyProvider(scriptEngine, body);
            } else if (ALWAYS.equals(streaming)) {
                return new StreamBodyProvider(scriptEngine, body);
            } else if (AUTO.equals(streaming)){
                return new AutoStreamBodyProvider(scriptEngine, body);
            } else {
                throw new IllegalArgumentException(format("Body provider not available for streaming mode '%s'", streaming));
            }
        }
    }
}
