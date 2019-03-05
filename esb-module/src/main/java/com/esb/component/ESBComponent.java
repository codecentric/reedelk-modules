package com.esb.component;

import com.esb.api.component.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ESBComponent {

    private static final Map<String, Class<? extends Component>> COMPONENTS;

    static {
        Map<String, Class<? extends Component>> tmp = new HashMap<>();
        tmp.put(Fork.class.getName(), Fork.class);
        tmp.put(Stop.class.getName(), Stop.class);
        tmp.put(Choice.class.getName(), Choice.class);
        tmp.put(FlowReference.class.getName(), FlowReference.class);
        COMPONENTS = Collections.unmodifiableMap(tmp);
    }

    private ESBComponent() {
    }

    public static boolean is(String componentName) {
        return COMPONENTS.containsKey(componentName);
    }

    public static boolean is(Component componentObject) {
        return COMPONENTS.containsKey(componentObject.getClass().getName());
    }

    public static Class<? extends Component> getDefiningClass(String componentName) {
        return COMPONENTS.get(componentName);
    }

    public static Collection<String> allNames() {
        return COMPONENTS.keySet();
    }
}
