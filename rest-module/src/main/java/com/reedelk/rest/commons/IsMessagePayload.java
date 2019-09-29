package com.reedelk.rest.commons;

import com.reedelk.runtime.api.script.DynamicValue;

public class IsMessagePayload {
    public static boolean from(DynamicValue<?> body) {
        return body != null &&
                body.isScript() &&
                body.isEvaluateMessagePayload();
    }
}
