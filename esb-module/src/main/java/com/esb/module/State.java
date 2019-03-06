package com.esb.module;

import com.esb.flow.Flow;
import com.esb.module.state.ModuleState;

import java.util.Collection;

public interface State {

    Collection<Flow> flows();

    Collection<Exception> errors();

    Collection<String> resolvedComponents();

    Collection<String> unresolvedComponents();

    ModuleState state();

}
