package com.esb.foonnel.rest;

import com.esb.foonnel.api.AbstractInbound;
import com.esb.foonnel.api.FoonnelException;
import com.esb.foonnel.rest.http.RESTServer;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RESTListener.class, scope = PROTOTYPE)
public class RESTListener extends AbstractInbound {

    private int port;
    private String host;

    private RESTServer server;

    static {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
    }

    @Override
    public void onStart() {
         server = new RESTServer(port, host);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutdown() {
        try {
            this.server.stop();
        } catch (InterruptedException e) {
            throw new FoonnelException(e);
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

}
