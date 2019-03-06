package com.esb.lifecycle;

import org.osgi.framework.Bundle;

public abstract class AbstractStep<I, O> implements Step<I, O> {

    protected static final Void NOTHING = null;

    private Bundle bundle;

    @Override
    public Bundle bundle() {
        return bundle;
    }

    @Override
    public void bundle(Bundle bundle) {
        this.bundle = bundle;
    }

}
