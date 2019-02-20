package com.esb.foonnel.rest;

import com.esb.foonnel.api.AbstractInbound;
import com.esb.foonnel.api.Message;
import com.esb.foonnel.rest.http.Handler;
import com.esb.foonnel.rest.http.RESTServer;
import com.esb.foonnel.rest.http.Request;
import com.esb.foonnel.rest.http.Response;
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
        RESTServer server = provider.get(host, port);
        server.addRoute(method, path, request -> {
            String body = request.body();
            Message message = new Message();
            message.setContent(body);
            Message output = onEvent(message);
            return new Response(output.getContent());
        });
    }

    @Override
    public void onShutdown() {
        RESTServer server = provider.get(host, port);
        server.removeRoute(method, path);
        try {
            provider.release(server);
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

}
