package com.reedelk.rest.client.header;

import com.reedelk.rest.commons.ContentType;
import com.reedelk.rest.commons.HttpHeader;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.NMapEvaluation;
import com.reedelk.runtime.api.service.ScriptEngineService;

import java.util.HashMap;
import java.util.Map;

public class HeadersEvaluator {

    private ScriptEngineService scriptEngine;
    private Map<String, String> userHeaders;

    private HeadersEvaluator() {
    }

    public HeaderProvider provider(Message message, FlowContext flowContext) {
        Map<String,String> headers = new HashMap<>();
        ContentType.from(message)
                .ifPresent(contentType -> headers.put(HttpHeader.CONTENT_TYPE, contentType));

        if (!userHeaders.isEmpty()) {
            // User-defined headers: interpret and add them
            NMapEvaluation<String> evaluation = scriptEngine.evaluate(message, flowContext, userHeaders);
            Map<String, String> evaluatedHeaders = evaluation.map(0);
            headers.putAll(evaluatedHeaders);
        }
        return () -> headers;
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
            evaluator.userHeaders = headers;
            return evaluator;
        }
    }
}
