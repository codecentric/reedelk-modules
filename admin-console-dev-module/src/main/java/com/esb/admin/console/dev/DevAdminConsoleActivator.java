package com.esb.admin.console.dev;

import com.esb.admin.console.dev.resources.*;
import com.esb.api.service.ConfigurationService;
import com.esb.internal.api.SystemProperty;
import com.esb.internal.api.module.v1.ModuleService;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = DevAdminConsoleActivator.class, scope = SINGLETON, immediate = true)
public class DevAdminConsoleActivator {

    private static final Logger logger = LoggerFactory.getLogger(DevAdminConsoleActivator.class);

    private static final int DEFAULT_LISTENING_PORT = 9988;

    private static final String CONFIG_KEY_LISTENING_PORT = "listening.port";
    private static final String CONFIG_PID = "com.esb.admin.console.dev";

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


        service = new DevAdminConsoleService(listeningPort,
                new HealthResources(systemProperty),
                new ModuleResources(moduleService),
                new ConsoleCSSResource(),
                new ConsoleHTMLResource(),
                new ConsoleJavascriptResource(),
                new ConsoleIndexResource());
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
