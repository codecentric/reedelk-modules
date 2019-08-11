package com.reedelk.esb.flow;

import com.reedelk.esb.component.ESBComponent;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.graph.ExecutionNode.ReferencePair;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static com.reedelk.esb.commons.ServiceReferenceProperty.COMPONENT_NAME;
import static org.osgi.framework.Bundle.ACTIVE;

public class ReleaseReferenceConsumer implements Consumer<ExecutionNode> {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseReferenceConsumer.class);

    private final Bundle bundle;

    private ReleaseReferenceConsumer(Bundle bundle) {
        this.bundle = bundle;
    }

    public static ReleaseReferenceConsumer get(Bundle bundle) {
        return new ReleaseReferenceConsumer(bundle);
    }

    @Override
    public void accept(ExecutionNode executionNode) {

        ReferencePair<Component> componentReference = executionNode.getComponentReference();
        Component component = componentReference.getImplementor();

        if (ESBComponent.is(component)) {
            // A component does not have an associated OSGi
            // service reference and service object because It is just
            // a java class instantiated by core.
            executionNode.clearReferences();
            return;
        }

        // Bundle state Active means STARTED
        if (bundle.getState() == ACTIVE) {
            BundleContext context = bundle.getBundleContext();
            // Release component and dependent implementors's OSGi references.
            unregisterOSGiReferences(context, componentReference);
            executionNode.getDependencyReferences().forEach(node -> unregisterOSGiReferences(context, node));
        }

        executionNode.clearReferences();

    }

    private <T extends Implementor> void unregisterOSGiReferences(BundleContext context, ExecutionNode.ReferencePair<T> referencePair) {
        ServiceReference<T> serviceReference = referencePair.getServiceReference();
        ServiceObjects<T> serviceObjects = context.getServiceObjects(serviceReference);
        serviceObjects.ungetService(referencePair.getImplementor()); // TODO: What if this one throws something? I think we should try-catch to minimize issues

        boolean released = context.ungetService(serviceReference);
        if (released) warnServiceNotReleased(serviceReference);
    }

    void warnServiceNotReleased(ServiceReference serviceReference) {
        logger.warn("Service Reference {} could not be released", COMPONENT_NAME.get(serviceReference));
    }
}
