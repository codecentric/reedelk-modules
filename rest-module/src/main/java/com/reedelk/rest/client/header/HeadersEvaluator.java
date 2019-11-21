package com.reedelk.rest.client.header;

import com.reedelk.rest.commons.ContentType;
import com.reedelk.rest.commons.IsEvaluateMessagePayload;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicmap.DynamicStringMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;

import java.util.HashMap;
import java.util.Map;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;

public class HeadersEvaluator {

    private ScriptEngineService scriptEngine;
    private DynamicStringMap userHeaders;
    private DynamicByteArray body;

    private HeadersEvaluator(ScriptEngineService scriptEngine,
                             DynamicStringMap userHeaders,
                             DynamicByteArray body) {
        this.scriptEngine = scriptEngine;
        this.userHeaders = userHeaders;
        this.body = body;
    }

    public HeaderProvider provider(Message message, FlowContext flowContext) {
        Map<String, String> headers = new HashMap<>();

        if (IsEvaluateMessagePayload.from(body)) {
            ContentType.from(message)
                    .ifPresent(contentType -> headers.put(CONTENT_TYPE, contentType));
        }

        if (!userHeaders.isEmpty()) {
            // User-defined headers: interpret and add them
            Map<String, String> evaluatedHeaders = scriptEngine.evaluate(userHeaders, flowContext, message);
            headers.putAll(evaluatedHeaders);
        }

        return () -> headers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ClientConfiguration configuration;
        private ScriptEngineService scriptEngine;
        private DynamicStringMap headers;
        private DynamicByteArray body;

        public Builder configuration(ClientConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder headers(DynamicStringMap headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(DynamicByteArray body) {
            this.body = body;
            return this;
        }

        public HeadersEvaluator build() {
            return new HeadersEvaluator(scriptEngine, headers, body);
        }
    }
}
