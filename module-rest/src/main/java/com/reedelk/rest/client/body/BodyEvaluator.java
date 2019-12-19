package com.reedelk.rest.client.body;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;

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
        private RestMethod method;
        private DynamicByteArray body;

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder method(RestMethod method) {
            this.method = method;
            return this;
        }

        public Builder body(DynamicByteArray body) {
            this.body = body;
            return this;
        }

        public BodyEvaluator build() {
            BodyProvider provider = method.hasBody() ?
                    new DefaultBodyProvider(scriptEngine, body) :
                    EmptyBodyProvider.INSTANCE;
            return new BodyEvaluator(provider);
        }
    }
}
