package com.reedelk.esb.graph;

import com.reedelk.esb.commons.ComponentDisposer;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;

public class ExecutionNode {

    private ComponentDisposer disposer;
    private ReferencePair<Component> componentReference;
    private List<ReferencePair<Implementor>> dependencyReferences = new ArrayList<>();

    public ExecutionNode(ComponentDisposer disposer, ReferencePair<Component> componentReference) {
        this.disposer = disposer;
        this.componentReference = componentReference;
    }

    public ReferencePair<Component> getComponentReference() {
        return componentReference;
    }

    public void add(ReferencePair<Implementor> dependencyReference) {
        this.dependencyReferences.add(dependencyReference);
    }

    public Component getComponent() {
        return componentReference.implementor;
    }

    public List<ReferencePair<Implementor>> getDependencyReferences() {
        return dependencyReferences;
    }

    public void clearReferences() {
        dispose(componentReference);
        dependencyReferences.forEach(this::dispose);
        dependencyReferences.clear();

        // we help the gc to faster identify which objects can be garbage collected.
        disposer = null;
        componentReference = null;
        dependencyReferences = null;
    }

    public boolean isUsingComponent(final String targetComponentName) {
        String componentName = componentNameOf(componentReference);
        if (componentName.equals(targetComponentName)) return true;

        return dependencyReferences
                .stream()
                .anyMatch(referencePair -> componentNameOf(referencePair)
                        .equals(targetComponentName));
    }

    private String componentNameOf(ReferencePair<? extends Implementor> referencePair) {
        return referencePair.getImplementor().getClass().getName();
    }

    private void dispose(ReferencePair<? extends Implementor> componentReference) {
        Implementor implementor = componentReference.implementor;
        if (implementor instanceof Component) {
            disposer.dispose((Component) implementor);
        }
        componentReference.implementor = null;
        componentReference.serviceReference = null;
    }


    public static class ReferencePair<T extends Implementor> {

        private T implementor;
        private ServiceReference<T> serviceReference;

        public ReferencePair(T implementor) {
            this.implementor = implementor;
            this.serviceReference = null;
        }

        public ReferencePair(T implementor, ServiceReference<T> serviceReference) {
            this.implementor = implementor;
            this.serviceReference = serviceReference;
        }

        public T getImplementor() {
            return implementor;
        }

        public ServiceReference<T> getServiceReference() {
            return serviceReference;
        }
    }
}