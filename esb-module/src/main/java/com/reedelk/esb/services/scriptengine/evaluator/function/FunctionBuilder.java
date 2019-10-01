package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.script.dynamicmap.DynamicMap;

public interface FunctionBuilder {

    default String build(String functionName, String functionBody) {
        throw new UnsupportedOperationException();
    }

    default <T> String build(String functionName, DynamicMap<T> map) {
        throw new UnsupportedOperationException();
    }
}
