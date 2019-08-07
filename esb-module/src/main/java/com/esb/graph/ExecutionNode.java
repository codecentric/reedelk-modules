package com.esb.graph;

import com.esb.api.component.Component;
import com.esb.api.component.Implementor;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;

public class ExecutionNode {

    private final ReferencePair<Component> componentReference;
    private final List<ReferencePair<Implementor>> dependencyReferences = new ArrayList<>();

    public ExecutionNode(ReferencePair<Component> componentReference) {
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
        componentReference.implementor = null;
        componentReference.serviceReference = null;

        dependencyReferences.forEach(implementorReferencePair -> {
            implementorReferencePair.implementor = null;
            implementorReferencePair.serviceReference = null;
        });
        dependencyReferences.clear();
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