package com.reedelk.esb.services.scriptengine.evaluator;

import org.reactivestreams.Publisher;

public interface DynamicValueConverter<Input,Output> {

    Output from(Input value);

    Publisher<Output> from(Publisher<Input> stream);
}
