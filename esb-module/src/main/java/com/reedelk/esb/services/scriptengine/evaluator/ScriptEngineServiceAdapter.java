package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;

import java.util.Map;
import java.util.Optional;

public class ScriptEngineServiceAdapter implements ScriptEngineService {

    // Dynamic value

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> value, Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> value, Throwable throwable, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> value, Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> value, Throwable throwable, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    // Script

    @Override
    public <T> Optional<T> evaluate(Script script, Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Publisher<T> evaluateStream(Script script, Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    // Dynamic map

    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, Message message, FlowContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDisposed(Component component) {
        throw new UnsupportedOperationException();
    }
}
