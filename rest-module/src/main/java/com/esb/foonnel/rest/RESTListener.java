package com.esb.foonnel.rest;

import com.esb.foonnel.api.AbstractInbound;
import com.esb.foonnel.api.Message;
import com.esb.foonnel.rest.http.Handler;
import com.esb.foonnel.rest.http.RESTServer;
import com.esb.foonnel.rest.http.ServerProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RESTListener.class, scope = PROTOTYPE)
public class RESTListener extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(RESTListener.class);

    private int port;
    private String host;
    private String path;
    private String method;

    private static ServerProvider provider = new ServerProvider();


    private ServerProvider serverProvider() {
        return provider;
    }

    @Override
    public void onStart() {

        RESTServer server = serverProvider().get(host, port);
        server.addRoute(method, path, defaultHandler);

        try {
            server.start();
        } catch (Exception e) {
            logger.error("Error while starting RESTListener", e);
        }
    }

    @Override
    public void onShutdown() {
        RESTServer server = serverProvider().get(host, port);
        server.removeRoute(method, path);
        try {
            server.stop();
        } catch (InterruptedException e) {
            logger.error("Error while stopping RESTListener", e);
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

    private final Handler defaultHandler = (request, response) -> {
        String body = request.body();
        Message message = new Message();
        message.setContent(body);
        return onEvent(message);
    };
}
