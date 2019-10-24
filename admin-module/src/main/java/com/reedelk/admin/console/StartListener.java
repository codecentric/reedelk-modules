package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.service.ConfigurationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Start Listener")
@Component(service = StartListener.class, scope = PROTOTYPE)
public class StartListener extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(StartListener.class);

    private static final String PROPERTY_ADMIN_CONSOLE_ADDRESS = "admin.console.address";
    private static final String PROPERTY_ADMIN_CONSOLE_PORT = "admin.console.port";

    @Reference
    private ConfigurationService configurationService;

    @Override
    public void onStart() {

        String bindAddress = configurationService.getString(PROPERTY_ADMIN_CONSOLE_ADDRESS);

        int bindPort = configurationService.getInt( PROPERTY_ADMIN_CONSOLE_PORT);

        logger.info(String.format("Admin console listening on http://%s:%d/console", bindAddress, bindPort));
    }

    @Override
    public void onShutdown() {
        // Nothing to do.
    }
}
