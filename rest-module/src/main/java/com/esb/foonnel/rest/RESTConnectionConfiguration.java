package com.esb.foonnel.rest;

import com.esb.foonnel.api.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RESTConnectionConfiguration.class, scope = PROTOTYPE)
public class RESTConnectionConfiguration implements Implementor {

    private int port;
    private String hostname;
    private String protocol;

    public void setPort(int port) {
        this.port = port;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }

    public String getProtocol() {
        return protocol;
    }
}
