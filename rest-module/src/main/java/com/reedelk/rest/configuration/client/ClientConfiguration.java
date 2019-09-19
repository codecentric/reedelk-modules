package com.reedelk.rest.configuration.client;

import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Shared
@Component(service = ClientConfiguration.class, scope = PROTOTYPE)
public class ClientConfiguration implements Implementor {

    @Hidden
    @Property("id")
    private String id;

    // Base URL config
    @Property("Host")
    @Default("localhost")
    private String host;

    @Property("Port")
    @Default("80")
    private Integer port;

    @Property("Base path")
    private String basePath;

    @Property("Protocol")
    private HttpProtocol protocol;

    // Default request config
    @Property("Request timeout")
    private Integer requestTimeout;

    @Property("Connect timeout")
    private Integer connectTimeout;

    @Property("Keep alive")
    @Default("true")
    private Boolean keepAlive;

    @Property("Follow redirects")
    @Default("true")
    private Boolean followRedirects;

    @Property("Content compression")
    private Boolean contentCompression;

    @Property("Expect continue")
    private Boolean expectContinue;

    @Property("Authentication")
    @Default("NONE")
    private Authentication authentication;

    @Property("Basic authentication")
    @When(propertyName = "authentication", propertyValue = "BASIC")
    private BasicAuthenticationConfiguration basicAuthentication;

    @Property("Digest authentication")
    @When(propertyName = "authentication", propertyValue = "DIGEST")
    private DigestAuthenticationConfiguration digestAuthentication;

    @Property("Proxy")
    @Default("NONE")
    private Proxy proxy;

    @Property("Proxy configuration")
    @When(propertyName = "proxy", propertyValue = "PROXY")
    private ProxyConfiguration proxyConfiguration;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
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

    public Boolean getFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(Boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public Boolean getContentCompression() {
        return contentCompression;
    }

    public void setContentCompression(Boolean contentCompression) {
        this.contentCompression = contentCompression;
    }

    public Boolean getExpectContinue() {
        return expectContinue;
    }

    public void setExpectContinue(Boolean expectContinue) {
        this.expectContinue = expectContinue;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public BasicAuthenticationConfiguration getBasicAuthentication() {
        return basicAuthentication;
    }

    public void setBasicAuthentication(BasicAuthenticationConfiguration basicAuthentication) {
        this.basicAuthentication = basicAuthentication;
    }

    public DigestAuthenticationConfiguration getDigestAuthentication() {
        return digestAuthentication;
    }

    public void setDigestAuthentication(DigestAuthenticationConfiguration digestAuthentication) {
        this.digestAuthentication = digestAuthentication;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public ProxyConfiguration getProxyConfiguration() {
        return proxyConfiguration;
    }

    public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
    }
}
