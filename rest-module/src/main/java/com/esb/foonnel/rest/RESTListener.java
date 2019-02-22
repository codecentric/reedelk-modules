package com.esb.foonnel.rest;

import com.esb.foonnel.api.AbstractInbound;
import com.esb.foonnel.rest.http.Server;
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

    private int port;
    private String host;
    private String path;
    private String method;


    @Override
    public void onStart() {
        Server server = provider.get(host, port);
        server.addRoute(method, path, this::onEvent);
    }

    @Override
    public void onShutdown() {
        Server server = provider.get(host, port);
        server.removeRoute(method, path);
        try {
            provider.release(server);
        } catch (InterruptedException e) {
            logger.error("Shutdown RESTListener", e);
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
