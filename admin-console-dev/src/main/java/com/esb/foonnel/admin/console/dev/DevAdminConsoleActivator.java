package com.esb.foonnel.admin.console.dev;

import com.esb.foonnel.admin.console.dev.resources.HealthResources;
import com.esb.foonnel.admin.console.dev.resources.ModuleResources;
import com.esb.foonnel.api.service.ConfigurationService;
import com.esb.foonnel.internal.api.SystemProperty;
import com.esb.foonnel.internal.api.module.v1.ModuleService;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.takes.facets.fork.Fork;

import java.util.Timer;
import java.util.TimerTask;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = DevAdminConsoleActivator.class, scope = SINGLETON, immediate = true)
public class DevAdminConsoleActivator {

    private static final Logger logger = LoggerFactory.getLogger(DevAdminConsoleActivator.class);

    private static final int DEFAULT_LISTENING_PORT = 9988;

    private static final String CONFIG_KEY_LISTENING_PORT = "listening.port";
    private static final String CONFIG_PID = "com.esb.foonnel.admin.console.dev";

    @Reference
    public SystemProperty systemProperty;
    @Reference
    public ModuleService moduleService;
    @Reference
    public ConfigurationService configurationService;

    private DevAdminConsoleService service;

    @Activate
    public void activate() throws BundleException {
        int listeningPort = configurationService.getIntConfigProperty(CONFIG_PID, CONFIG_KEY_LISTENING_PORT, DEFAULT_LISTENING_PORT);

        Fork healthResources = new HealthResources(systemProperty);
        Fork deploymentResources = new ModuleResources(moduleService);

        service = new DevAdminConsoleService(listeningPort, healthResources, deploymentResources);
        service.start();

        // TODO: Fix this logger. Configuration should wait until completed.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info(String.format("Dev Admin Console listening on port %d", listeningPort));
            }
        }, 500);
    }

    @Deactivate
    public void deactivate() {
        service.stop();
    }

}
