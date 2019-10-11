package com.reedelk.rest.configuration.listener;

import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Shared
@Component(service = ListenerConfiguration.class, scope = PROTOTYPE)
public class ListenerConfiguration implements Implementor {

    @Property("Host")
    @Default("localhost")
    @Hint("localhost")
    private String host;

    @Property("Port")
    @Default("8080")
    @Hint("8080")
    private Integer port;

    @Property("Protocol")
    @Default("HTTP")
    private HttpProtocol protocol;

    @Property("Base path")
    @Hint("/api")
    private String basePath;

    @Property("Keep alive")
    @Default("true")
    private Boolean keepAlive;

    @Property("Compress response")
    @Default("true")
    private Boolean compress;

    @Property("Read timeout millis")
    private Integer readTimeoutMillis;

    @Property("Connection timeout millis")
    private Integer connectionTimeoutMillis;

    @Property("Socket backlog")
    private Integer socketBacklog;

    @Property("Max chunk size")
    private Integer maxChunkSize;

    @Property("Max headers length")
    private Integer maxLengthOfAllHeaders;

    @Property("Security configuration")
    @When(propertyName = "protocol", propertyValue = "HTTPS")
    private SecurityConfiguration securityConfiguration;

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

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Integer getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(Integer readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public Integer getConnectionTimeoutMillis() {
        return connectionTimeoutMillis;
    }

    public void setConnectionTimeoutMillis(Integer connectionTimeoutMillis) {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
    }

    public Boolean getCompress() {
        return compress;
    }

    public void setCompress(Boolean compress) {
        this.compress = compress;
    }

    public Integer getSocketBacklog() {
        return socketBacklog;
    }

    public void setSocketBacklog(Integer socketBacklog) {
        this.socketBacklog = socketBacklog;
    }

    public Integer getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(Integer maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public Integer getMaxLengthOfAllHeaders() {
        return maxLengthOfAllHeaders;
    }

    public void setMaxLengthOfAllHeaders(Integer maxLengthOfAllHeaders) {
        this.maxLengthOfAllHeaders = maxLengthOfAllHeaders;
    }

    public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
