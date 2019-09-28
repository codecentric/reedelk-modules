package com.reedelk.esb.services.scriptengine.evaluator;

public interface DynamicValueConverter<Input,Output> {
    Output to(Input value);
}
