package com.reedelk.scheduler;

import com.reedelk.scheduler.commons.SchedulerProvider;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = SchedulerModuleActivator.class, scope = SINGLETON, immediate = true)
public class SchedulerModuleActivator {

    @Activate
    public void activate() throws BundleException {
    }

    @Deactivate
    public void deactivate() {
        SchedulerProvider.dispose();
    }
}
