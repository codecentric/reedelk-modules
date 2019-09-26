package com.reedelk.rest.client.header;

import com.reedelk.rest.commons.ContentType;
import com.reedelk.rest.commons.IsMessagePayload;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicByteArray;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.service.ScriptEngineService;

import java.util.HashMap;
import java.util.Map;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;

public class HeadersEvaluator {

    private ScriptEngineService scriptEngine;
    private DynamicMap<String> userHeaders;
    private DynamicByteArray body;

    private HeadersEvaluator() {
    }

    public HeaderProvider provider(Message message, FlowContext flowContext) {
        Map<String, String> headers = new HashMap<>();

        if (IsMessagePayload.from(body)) {
            ContentType.from(message)
                    .ifPresent(contentType -> headers.put(CONTENT_TYPE, contentType));
        }

        if (!userHeaders.isEmpty()) {
            // User-defined headers: interpret and add them
            Map<String, String> evaluatedHeaders = scriptEngine.evaluate(message, flowContext, userHeaders);
            headers.putAll(evaluatedHeaders);
        }

        return () -> headers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ScriptEngineService scriptEngine;
        private DynamicMap<String> headers;
        private DynamicByteArray body;

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder headers(DynamicMap<String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(DynamicByteArray body) {
            this.body = body;
            return this;
        }

        public HeadersEvaluator build() {
            HeadersEvaluator evaluator = new HeadersEvaluator();
            evaluator.scriptEngine = scriptEngine;
            evaluator.userHeaders = headers;
            evaluator.body = body;
            return evaluator;
        }
    }
}
