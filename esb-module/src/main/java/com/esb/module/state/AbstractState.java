package com.esb.module.state;

import com.esb.flow.Flow;
import com.esb.module.State;

import java.util.Collection;

abstract class AbstractState implements State {

    @Override
    public Collection<Flow> flows() {
        throw new UnsupportedOperationException("flows");
    }

    @Override
    public Collection<Exception> errors() {
        throw new UnsupportedOperationException("errors");
    }

    @Override
    public Collection<String> resolvedComponents() {
        throw new UnsupportedOperationException("resolvedComponents");
    }

    @Override
    public Collection<String> unresolvedComponents() {
        throw new UnsupportedOperationException("unresolvedComponents");
    }
}
