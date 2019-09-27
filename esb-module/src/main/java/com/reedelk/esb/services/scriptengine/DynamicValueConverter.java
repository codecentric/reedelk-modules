package com.reedelk.esb.services.scriptengine;

public interface DynamicValueConverter<Input,Output> {
    Output to(Input value);
}
