package com.esb.rest.component;

import com.esb.api.annotation.Default;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Shareable;
import com.esb.api.component.Implementor;
import com.esb.rest.commons.HttpProtocol;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Shareable
@Component(service = RestListenerConfiguration.class, scope = PROTOTYPE)
public class RestListenerConfiguration implements Implementor {

    @Property("Hostname")
    @Default("localhost")
    private String hostname;

    @Property("Port")
    @Default("8080")
    private int port;

    @Property("Protocol")
    @Default("HTTP_1_1")
    private HttpProtocol protocol;
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

    @Property("Max Chunk Size")
    private Integer maxChunkSize;

    @Property("Max initial line length")
    private Integer maxInitialLineLength;

    @Property("Max length of all headers")
    private Integer maxLengthOfAllHeaders;

    @Property("Max content size")
    private Integer maxContentSize;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    public Integer getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public void setMaxInitialLineLength(Integer maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
    }

    public Integer getMaxLengthOfAllHeaders() {
        return maxLengthOfAllHeaders;
    }

    public void setMaxLengthOfAllHeaders(Integer maxLengthOfAllHeaders) {
        this.maxLengthOfAllHeaders = maxLengthOfAllHeaders;
    }

    public Integer getMaxContentSize() {
        return maxContentSize;
    }

    public void setMaxContentSize(Integer maxContentSize) {
        this.maxContentSize = maxContentSize;
    }
}
