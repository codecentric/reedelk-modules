package com.reedelk.rest.commons;

import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;

public class IsMessagePayload {

    private IsMessagePayload() {
    }
    
    public static boolean from(DynamicValue<?> body) {
        return body != null &&
                body.isScript() &&
                body.isEvaluateMessagePayload();
    }
}
