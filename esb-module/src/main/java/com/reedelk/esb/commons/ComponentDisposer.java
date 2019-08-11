package com.reedelk.esb.commons;

import com.reedelk.runtime.api.component.Component;

public class ComponentDisposer {
    public static void dispose(Component component) {
        if (component != null) {
            component.dispose();
        }
    }
}
