package com.reedelk.rabbitmq.configuration;

import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Shared;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Shared
@Component(service = ConnectionFactoryConfiguration.class, scope = PROTOTYPE)
public class ConnectionFactoryConfiguration implements Implementor {

    @Property("Username")
    @Hint("guest")
    private String userName;

    @Property("Password")
    @Hint("guest")
    private String password;

    @Property("Virtual Host")
    @Hint("/")
    private String virtualHost;

    @Property("Host Name")
    @Hint("localhost")
    private String hostName;

    @Property("Port Number")
    @Hint("5672")
    private Integer portNumber;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }
}
