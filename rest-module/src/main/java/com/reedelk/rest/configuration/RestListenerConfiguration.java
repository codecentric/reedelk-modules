package com.reedelk.rest.configuration;

import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Shareable;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Shareable
@Component(service = RestListenerConfiguration.class, scope = PROTOTYPE)
public class RestListenerConfiguration implements Implementor {

    @Property("Host")
    @Default("localhost")
    private String host;

    @Property("Port")
    @Default("8080")
    private int port;

    @Property("Protocol")
    @Default("HTTP")
    private HttpProtocol protocol;

    @Property("Base path")
    private String basePath;

    @Property("Keep alive")
    @Default("true")
    private Boolean keepAlive;

    @Property("Read timeout millis")
    private Integer readTimeoutMillis;

    @Property("Connection Timeout Millis")
    private Integer connectionTimeoutMillis;

    @Property("Socket Backlog")
    private Integer socketBacklog;

    @Property("Validate headers")
    private Boolean validateHeaders;

    @Property("Compress response")
    private Boolean compress;

    @Property("Max Chunk Size")
    private Integer maxChunkSize;

    @Property("Max length of all headers")
    private Integer maxLengthOfAllHeaders;

    @Property("Security configuration")
    private SecurityConfiguration securityConfiguration;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
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

    public Boolean getValidateHeaders() {
        return validateHeaders;
    }

    public void setValidateHeaders(Boolean validateHeaders) {
        this.validateHeaders = validateHeaders;
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
