package com.esb.rest.component;

import com.esb.api.annotation.Default;
import com.esb.api.annotation.Property;
import com.esb.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RestListenerConfiguration.class, scope = PROTOTYPE)
public class RestListenerConfiguration implements Implementor {

    @Property("Hostname")
    @Default("localhost")
    private String hostname;

    @Property("Port")
    @Default("8080")
    private int port;

    @Property("Protocol")
    @Default("HTTP/1.1")
    private String protocol = "HTTP/1.1";

    @Property("Socket Backlog")
    @Default("128")
    private int socketBacklog;

    @Property("Max Chunk Size")
    @Default("8192") // in bytes
    private int maxChunkSize;

    @Property("Max initial line length")
    @Default("4096") // in bytes
    private Integer maxInitialLineLength;

    @Property("Max length of all headers")
    @Default("8192") // in bytes
    private Integer maxLengthOfAllHeaders;

    @Property("Max content size")
    @Default("104857600") // 100 MB (100 * 1024 * 1024)
    private Integer maxContentSize;

    @Property("Read timeout millis")
    @Default("30000") // 30 seconds (30 * 1000)
    private int readTimeoutMillis;

    @Property("Connection Timeout Millis")
    private int connectionTimeoutMillis;

    @Property("Keep alive")
    @Default("true")
    private Boolean keepAlive;

    @Property("Validate headers")
    @Default("false")
    private Boolean validateHeaders;


    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Boolean getValidateHeaders() {
        return validateHeaders;
    }

    public void setValidateHeaders(Boolean validateHeaders) {
        this.validateHeaders = validateHeaders;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

}
