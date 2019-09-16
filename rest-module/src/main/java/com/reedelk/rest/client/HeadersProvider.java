package com.reedelk.rest.client;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.NMapEvaluation;
import com.reedelk.runtime.api.service.ScriptEngineService;

import java.util.Map;

public class HeadersProvider {

    public static HeaderProvider from(Map<String, String> headers, Message message, FlowContext flowContext, ScriptEngineService scriptEngine) {
        return httpHeaders -> {
            if (!headers.isEmpty()) {
                // User-defined headers: interpret and add them
                NMapEvaluation<String> evaluation =
                        scriptEngine.evaluate(message, flowContext, headers);
                Map<String, String> evaluatedHeaders = evaluation.map(1);
                evaluatedHeaders.forEach(httpHeaders::set);
            }
        };
    }
}
