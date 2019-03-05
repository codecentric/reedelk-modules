package com.esb.services.event;

import com.esb.commons.ModuleProperties;
import org.osgi.framework.*;

import java.util.Dictionary;

import static com.esb.commons.Preconditions.checkArgument;
import static com.esb.commons.ServiceReferenceProperty.COMPONENT_NAME;
import static org.osgi.framework.BundleEvent.STARTED;
import static org.osgi.framework.BundleEvent.STOPPED;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

public class ESBEventService implements BundleListener, ServiceListener {

    private final EventListener listener;

    public ESBEventService(EventListener listener) {
        checkArgument(listener != null, "listener must not be null");
        this.listener = listener;
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (isNotModule(bundleEvent.getBundle())) return;

        long bundleId = bundleEvent.getBundle().getBundleId();

        if (STARTED == bundleEvent.getType()) {
            listener.moduleStarted(bundleId);
        }

        if (STOPPED == bundleEvent.getType()) {
            listener.moduleStopped(bundleId);
        }
    }

    @Override
    public void serviceChanged(ServiceEvent serviceEvent) {
        if (isNotModule(serviceEvent.getServiceReference().getBundle())) return;

        String componentName = COMPONENT_NAME.get(serviceEvent.getServiceReference());
        if (componentName == null) return;

        if (UNREGISTERING == serviceEvent.getType()) {
            listener.componentUnregistering(componentName);
        }

        if (REGISTERED == serviceEvent.getType()) {
            listener.componentRegistered(componentName);
        }
    }

    private static boolean isNotModule(Bundle bundle) {
        Dictionary<String, String> bundleHeaders = bundle.getHeaders();
        String isModule = bundleHeaders.get(ModuleProperties.Bundle.MODULE_HEADER_NAME);
        return !Boolean.parseBoolean(isModule);
    }
}
