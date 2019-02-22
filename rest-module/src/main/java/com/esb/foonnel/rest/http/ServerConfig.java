package com.esb.foonnel.rest.http;

public class ServerConfig {

    private final int port;
    private final String hostname;
    private final String httpVersion; // (protocol)

    public ServerConfig(String hostname, int port, String httpVersion) {
        this.port = port;
        this.hostname = hostname;
        this.httpVersion = httpVersion;
    }

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}
