package com.reedelk.esb.commons;

import com.reedelk.esb.services.scriptengine.JavascriptEngine;

public class AddComponentDisposerListeners {

    public static void from(ComponentDisposer disposer, JavascriptEngine scriptEngine) {
        // Script engine needs to be notified when a component is disposed in order
        // to properly cleanup compiled scripts.
        disposer.addListener(scriptEngine::onDisposed);
    }
}
