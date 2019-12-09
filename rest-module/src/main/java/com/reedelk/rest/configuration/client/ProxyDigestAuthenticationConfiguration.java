package com.reedelk.rest.configuration.client;

import com.reedelk.runtime.api.annotation.Password;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = ProxyDigestAuthenticationConfiguration.class, scope = PROTOTYPE)
public class ProxyDigestAuthenticationConfiguration implements Implementor {

    @Property("Username")
    private String username;

    @Password
    @Property("Password")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
