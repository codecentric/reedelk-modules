package com.esb.commons;

import com.esb.api.component.Component;

public class ComponentDisposer {
    public static void dispose(Component component) {
        if (component != null) {
            component.dispose();
        }
    }
}
