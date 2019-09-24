package com.reedelk.esb.commons;

import com.reedelk.runtime.api.component.Component;

import java.util.ArrayList;
import java.util.List;

public class ComponentDisposer {

    private List<Listener> listeners = new ArrayList<>();

    void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void dispose(Component component) {
        if (component != null) {
            component.dispose();
            listeners.forEach(listener -> listener.onDisposed(component));
        }
    }

    public interface Listener {
        void onDisposed(Component component);
    }
}
