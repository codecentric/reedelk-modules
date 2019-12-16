package com.reedelk.rest;

import com.reedelk.rest.client.DefaultHttpClientFactory;
import com.reedelk.rest.client.HttpClientFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = RestModuleActivator.class, scope = SINGLETON, immediate = true)
public class RestModuleActivator {

    private static final Dictionary<String, ?> NO_PROPERTIES = new Hashtable<>();

    private ServiceRegistration<HttpClientFactory> registration;

    @Activate
    public void activate(BundleContext context) throws BundleException {
        HttpClientFactory service = new DefaultHttpClientFactory();
        this.registration = context.registerService(HttpClientFactory.class, service, NO_PROPERTIES);
    }

    @Deactivate
    public void deactivate() {
        if (registration != null) registration.unregister();
    }
}
