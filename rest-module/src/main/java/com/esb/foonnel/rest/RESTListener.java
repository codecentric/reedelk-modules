package com.esb.foonnel.rest;

import com.esb.foonnel.api.component.AbstractInbound;
import com.esb.foonnel.rest.http.server.Server;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RESTListener.class, scope = PROTOTYPE)
public class RESTListener extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(RESTListener.class);

    @Reference
    private ServerProvider provider;

    private String path;
    private String method;
    private RESTConnectionConfiguration configuration;


    @Override
    public void onStart() {
        Server server = provider.get(configuration);
        server.addRoute(method, path, request -> onEvent(request));
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

    public void setMethod(String method) {
        this.method = method;
    }

    public void setConfiguration(RESTConnectionConfiguration configuration) {
        this.configuration = configuration;
    }
}
