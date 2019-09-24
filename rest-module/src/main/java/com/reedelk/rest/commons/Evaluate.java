package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.script.DynamicValue;

import static com.reedelk.runtime.api.commons.ScriptUtils.unwrap;

public class Evaluate {

    public static final DynamicValue ERROR = DynamicValue.from("#[error]");

    /**
     * Tests whether the script contains only the error payload.
     * @return true if the script evaluates the 'error', false otherwise.
     */
    public static boolean isErrorPayload(DynamicValue script) {
        String unwrappedScript = unwrap(script.getBody());
        return "error".equals(StringUtils.trim(unwrappedScript));
    }
}
