package com.esb.foonnel.rest.component;

import com.esb.foonnel.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RestListenerConfiguration.class, scope = PROTOTYPE)
public class RestListenerConfiguration implements Implementor {

    private String id;
    private String hostname;
    private String protocol = "HTTP/1.1";

    private Integer socketBacklog = 128;
    private Integer maxChunkSize = 8192; // in bytes
    private Integer maxInitialLineLength = 4096; // in bytes
    private Integer maxLengthOfAllHeaders = 8192; // in bytes
    private Integer maxContentSize = 100 * 1024 * 1024; // 100 mb
    private Integer readTimeoutMillis  = 30 * 1000; // 30 seconds

    private Integer port;
    private Integer connectionTimeoutMillis;

    private Boolean keepAlive = true;
    private Boolean validateHeaders = false;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
