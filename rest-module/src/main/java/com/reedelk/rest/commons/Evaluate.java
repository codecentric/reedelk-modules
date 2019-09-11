package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;

import static com.reedelk.runtime.api.commons.ScriptUtils.unwrap;

public class Evaluate {

    public static final String ERROR = "#[error]";

    /**
     * Tests whether the script contains only the error payload.
     * @return true if the script evaluates the 'error', false otherwise.
     */
    public static boolean isErrorPayload(String script) {
        String unwrappedScript = unwrap(script);
        return "error".equals(StringUtils.trim(unwrappedScript));
    }

}
