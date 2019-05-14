package com.esb.rest.component;

import com.esb.api.annotation.DisplayName;
import com.esb.api.annotation.EsbComponent;
import com.esb.api.annotation.Required;
import com.esb.api.component.AbstractInbound;
import com.esb.rest.commons.RestMethod;
import com.esb.rest.server.Server;
import com.esb.rest.server.ServerProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.esb.rest.commons.Preconditions.isNotNull;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@EsbComponent
@Component(service = RestListener.class, scope = PROTOTYPE)
public class RestListener extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(RestListener.class);

    @Reference
    private ServerProvider provider;

    @Required
    @DisplayName("REST Path")
    private String path;
    @Required
    @DisplayName("REST Method")
    private RestMethod method;

    private RestListenerConfiguration configuration;


    @Override
    public void onStart() {
        isNotNull(configuration, "Configuration was null");
        Server server = provider.get(configuration);
        server.addRoute(method, path, this::onEvent);
    }

    @Override
    public void onShutdown() {
        Server server = provider.get(configuration);
        server.removeRoute(method, path);
        try {
            provider.release(server);
        } catch (Exception e) {
            logger.error("Shutdown RESTListener", e);
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }

    public void setConfiguration(RestListenerConfiguration configuration) {
        this.configuration = configuration;
    }
}
