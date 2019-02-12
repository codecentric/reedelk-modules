package com.esb.foonnel.processor.http.inbound;

public class HttpServerConfiguration {

    private int port;
    private String bindAddress;

    public HttpServerConfiguration() {}

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }
}
