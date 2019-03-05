package com.esb.module;

public class Installed extends AbstractState {

    @Override
    public ModuleState state() {
        return ModuleState.INSTALLED;
    }
}
