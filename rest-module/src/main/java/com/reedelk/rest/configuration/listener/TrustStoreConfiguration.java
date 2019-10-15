package com.reedelk.rest.configuration.listener;

import com.reedelk.runtime.api.annotation.Password;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = TrustStoreConfiguration.class, scope = PROTOTYPE)
public class TrustStoreConfiguration implements Implementor {

    @Property("Path")
    private String path;
    @Password
    @Property("Password")
    private String password;
    @Property("Type")
    private String type;
    @Property("Algorithm")
    private String algorithm;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
