package com.reedelk.rest.configuration.client;

import com.reedelk.runtime.api.annotation.Password;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.When;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = DigestAuthenticationConfiguration.class, scope = PROTOTYPE)
public class DigestAuthenticationConfiguration implements Implementor {

    @Property("Username")
    private String username;

    @Password
    @Property("Password")
    private String password;

    @Property("Preemptive")
    private Boolean preemptive;

    @Property("Realm")
    @When(propertyName = "preemptive", propertyValue = "true")
    private String realm;

    @Property("Nonce")
    @When(propertyName = "preemptive", propertyValue = "true")
    private String nonce;

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

    public Boolean getPreemptive() {
        return preemptive;
    }

    public void setPreemptive(Boolean preemptive) {
        this.preemptive = preemptive;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
