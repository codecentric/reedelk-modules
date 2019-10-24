package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.Module;

public class TransitionToInstalled extends AbstractStep<Module, Module> {

    @Override
    public Module run(Module input) {
        input.installed();
        return input;
    }
}
