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
        tmp.put(Stop.class.getName(), Stop.class);
        tmp.put(Choice.class.getName(), ChoiceWrapper.class);
        tmp.put(Fork.class.getName(), ForkWrapper.class);
        tmp.put(FlowReference.class.getName(), FlowReference.class);
        COMPONENTS = Collections.unmodifiableMap(tmp);
    }

    private ESBComponent() {
    }

    public static boolean is(String componentName) {
        return COMPONENTS.containsKey(componentName);
    }

    public static boolean is(Component componentObject) {
        // TODO: Fix this
        return COMPONENTS.keySet().stream().anyMatch(componentClassName -> {
            try {
                Class<?> aClass = Class.forName(componentClassName);
                return aClass.isAssignableFrom(componentObject.getClass());
            } catch (ClassNotFoundException e) {
                return false;
            }
        });
    }

    public static Class<? extends Component> getDefiningClass(String componentName) {
        return COMPONENTS.get(componentName);
    }

    public static Collection<String> allNames() {
        return COMPONENTS.keySet();
    }
}
