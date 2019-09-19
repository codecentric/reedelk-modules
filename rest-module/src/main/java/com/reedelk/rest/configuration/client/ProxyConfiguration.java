package com.reedelk.rest.configuration.client;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.When;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = ProxyConfiguration.class, scope = PROTOTYPE)
public class ProxyConfiguration implements Implementor {

    @Property("Host")
    private String host;

    @Property("Port")
    @Default("8080")
    private Integer port;

    @Property("Authentication")
    @Default("NONE")
    private ProxyAuthentication authentication;

    @Property("Proxy authentication")
    @When(propertyName = "authentication", propertyValue = "USER_AND_PASSWORD")
    private ProxyAuthenticationConfiguration authenticationConfiguration;

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

    public ProxyAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(ProxyAuthentication authentication) {
        this.authentication = authentication;
    }

    public ProxyAuthenticationConfiguration getAuthenticationConfiguration() {
        return authenticationConfiguration;
    }

    public void setAuthenticationConfiguration(ProxyAuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }
}
