package com.reedelk.rest.client.header;

import com.reedelk.rest.client.HeaderProvider;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.NMapEvaluation;
import com.reedelk.runtime.api.service.ScriptEngineService;

import java.util.Map;

public class HeadersEvaluator {

    private ScriptEngineService scriptEngine;
    private Map<String, String> headers;

    private HeadersEvaluator() {
    }

    public HeaderProvider provider(Message message, FlowContext flowContext) {
        return headers.isEmpty() ?
                () -> headers :
                () -> {
                    // User-defined headers: interpret and add them
                    NMapEvaluation<String> evaluation = scriptEngine.evaluate(message, flowContext, headers);
                    return evaluation.map(0);
                };
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ScriptEngineService scriptEngine;
        private Map<String,String> headers;

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public HeadersEvaluator build() {
            HeadersEvaluator evaluator = new HeadersEvaluator();
            evaluator.scriptEngine = scriptEngine;
            evaluator.headers = headers;
            return evaluator;
        }
    }
}
