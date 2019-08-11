package com.reedelk.admin.console.dev;

import com.reedelk.admin.console.dev.resources.console.ConsoleCSSResource;
import com.reedelk.admin.console.dev.resources.console.ConsoleHTMLResource;
import com.reedelk.admin.console.dev.resources.console.ConsoleIndexResource;
import com.reedelk.admin.console.dev.resources.console.ConsoleJavascriptResource;
import com.reedelk.admin.console.dev.resources.health.HealthResources;
import com.reedelk.admin.console.dev.resources.hotswap.HotSwapResources;
import com.reedelk.admin.console.dev.resources.module.ModuleResources;
import com.reedelk.runtime.api.service.ConfigurationService;
import com.reedelk.runtime.system.api.HotSwapService;
import com.reedelk.runtime.system.api.ModuleService;
import com.reedelk.runtime.system.api.SystemProperty;
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

@Component(service = DevAdminConsole.class, scope = SINGLETON, immediate = true)
public class DevAdminConsole {

    private static final Logger logger = LoggerFactory.getLogger(DevAdminConsole.class);

    private static final int DEFAULT_LISTENING_PORT = 9988;
    private static final String DEFAULT_BIND_ADDRESS = "localhost";

    private static final String CONFIG_KEY_LISTENING_PORT = "admin.console.bind.port";
    private static final String CONFIG_KEY_LISTENING_ADDRESS = "admin.console.bind.address";
    private static final String CONFIG_PID = "com.reedelk.admin.console.dev";

    @Reference
    private ModuleService moduleService;
    @Reference
    private SystemProperty systemProperty;
    @Reference
    private HotSwapService hotSwapService;
    @Reference
    private ConfigurationService configurationService;

    private DevAdminConsoleService service;

    @Activate
    public void activate() throws BundleException {
        int listeningPort = configurationService.getIntConfigProperty(CONFIG_PID, CONFIG_KEY_LISTENING_PORT, DEFAULT_LISTENING_PORT);
        String bindAddress = configurationService.getStringConfigProperty(CONFIG_PID, CONFIG_KEY_LISTENING_ADDRESS, DEFAULT_BIND_ADDRESS);

        service = new DevAdminConsoleService(bindAddress, listeningPort,
                new HealthResources(systemProperty, "/api/health"),
                new ModuleResources(moduleService, "/api/module"),
                new HotSwapResources(hotSwapService, "/api/hotswap"),
                new ConsoleCSSResource(),
                new ConsoleHTMLResource(),
                new ConsoleJavascriptResource(),
                new ConsoleIndexResource());
        service.start();

        waitAndLogStarted(bindAddress, listeningPort);
    }

    @Deactivate
    public void deactivate() {
        service.stop();
    }

    private void waitAndLogStarted(String bindAddress, int listeningPort) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info(String.format("Dev admin console listening on http://%s:%d/console", bindAddress, listeningPort));
            }
        }, 500);
    }

}
