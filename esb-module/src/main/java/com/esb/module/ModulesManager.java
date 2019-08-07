package com.esb.module;

import com.esb.api.component.Component;
import com.esb.api.component.Implementor;
import com.esb.api.exception.ESBException;
import com.esb.component.ESBComponent;
import com.esb.graph.ExecutionNode;
import com.esb.graph.ExecutionNode.ReferencePair;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.esb.module.state.ModuleState.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class ModulesManager {

    private Map<Long, Module> idModulesMap = new HashMap<>();

    public void add(Module module) {
        long moduleId = module.id();
        idModulesMap.put(moduleId, module);
    }

    public void removeModuleById(long moduleId) {
        idModulesMap.remove(moduleId);
    }

    public boolean isModuleRegistered(long moduleId) {
        return idModulesMap.containsKey(moduleId);
    }

    public Module getModuleById(long moduleId) {
        return idModulesMap.get(moduleId);
    }

    public Collection<Module> allModules() {
        return idModulesMap.values();
    }

    public Collection<Module> findUnresolvedModules() {
        return idModulesMap.values().stream()
                .filter(module -> module.state() == UNRESOLVED)
                .collect(toList());
    }

    // We look only for modules in state: UNRESOLVED, RESOLVED, STOPPED, STARTED
    public Collection<Module> findModulesUsingComponent(String componentName) {
        return idModulesMap.values().stream()
                .filter(module -> module.state() != INSTALLED)
                .filter(module -> module.state() == UNRESOLVED || module.state() == RESOLVED || module.state() == ERROR ?
                        module.resolvedComponents().contains(componentName) :
                        module.flows().stream().anyMatch(flow -> flow.isUsingComponent(componentName)))
                .collect(toList());
    }

    public ExecutionNode instantiateComponent(final BundleContext context, final String componentName) {
        if (ESBComponent.is(componentName)) {
            Component component = instantiateSystemComponent(componentName);
            return new ExecutionNode(new ReferencePair<>(component));
        }

        ReferencePair<Component> implementorReferencePair = instantiateImplementor(context, componentName);
        return new ExecutionNode(implementorReferencePair);
    }

    public Implementor instantiateImplementor(final BundleContext context, final ExecutionNode executionNode, final String componentName) {
        ReferencePair<Implementor> implementorReferencePair = instantiateImplementor(context, componentName);
        executionNode.add(implementorReferencePair);
        return implementorReferencePair.getImplementor();
    }

    public <T extends Implementor> ReferencePair<T> instantiateImplementor(final BundleContext context, final String componentName) {
        Optional<ServiceReference<T>> optionalServiceReference = getImplementorReferenceByName(context, componentName);
        if (!optionalServiceReference.isPresent()) {
            throw new IllegalStateException(format("Expected Component %s was not found", componentName));
        }

        ServiceReference<T> serviceReference = optionalServiceReference.get();
        ServiceObjects<T> serviceObjects = context.getServiceObjects(serviceReference);
        T implementor = serviceObjects.getService();
        return new ReferencePair<>(implementor, serviceReference);
    }

    private Component instantiateSystemComponent(String componentName) {
        Class<? extends Component> systemComponentClass = ESBComponent.getDefiningClass(componentName);
        try {
            return systemComponentClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ESBException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Implementor> Optional<ServiceReference<T>> getImplementorReferenceByName(BundleContext bundleContext, String name) {
        return Optional.ofNullable((ServiceReference<T>) bundleContext.getServiceReference(name));
    }

}
