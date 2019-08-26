package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Shared;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Shared
@Component(service = RestClientConfiguration.class, scope = PROTOTYPE)
public class RestClientConfiguration implements Implementor {

    @Property("Host")
    @Default("localhost")
    private String host;

    @Property("Port")
    @Default("80")
    private Integer port;

    @Property("Protocol")
    @Default("HTTP")
    private HttpProtocol protocol;

    @Property("Base path")
    private String basePath;

    @Property("Keep alive")
    private Boolean keepAlive;

    @Property("Follow redirects")
    @Default("true")
    private Boolean followRedirects;

    @Property("Use persistent connections")
    private Boolean persistentConnections;

    @Property("Connection idle timeout")
    private Integer connectionIdleTimeout;

    @Property("Response buffer size")
    private Integer responseBufferSize;

    @Property("Authentication")
    @Default("NONE")
    private Authentication authentication;

    @Property("Proxy")
    @Default("NONE")
    private Proxy proxy;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public HttpProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(HttpProtocol protocol) {
        this.protocol = protocol;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Boolean getFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(Boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public Boolean getPersistentConnections() {
        return persistentConnections;
    }

    public void setPersistentConnections(Boolean persistentConnections) {
        this.persistentConnections = persistentConnections;
    }

    public Integer getConnectionIdleTimeout() {
        return connectionIdleTimeout;
    }

    public void setConnectionIdleTimeout(Integer connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }

    public Integer getResponseBufferSize() {
        return responseBufferSize;
    }

    public void setResponseBufferSize(Integer responseBufferSize) {
        this.responseBufferSize = responseBufferSize;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }
}
