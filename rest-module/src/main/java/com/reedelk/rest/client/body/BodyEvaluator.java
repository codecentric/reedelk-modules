package com.reedelk.rest.client.body;

import com.reedelk.rest.client.BodyProvider;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import reactor.core.publisher.Mono;

public class BodyEvaluator {

    private ScriptEngineService scriptEngine;
    private RestMethod method;
    private String body;

    private BodyEvaluator() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public BodyProvider provider(Message message, FlowContext flowContext) {
        if (method.hasBody()) {
            return () -> BodyStreamProvider.from(message, flowContext, body, scriptEngine);
        } else {
            // Method does not have any body.
            return Mono::empty;
        }
    }

    public static class Builder {

        private ScriptEngineService scriptEngine;
        private RestMethod method;
        private String body;

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
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
            evaluator.scriptEngine = scriptEngine;
            evaluator.method = method;
            evaluator.body = body;
            return evaluator;
        }
    }
}
