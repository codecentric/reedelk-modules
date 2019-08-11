package com.reedelk.mysql;

import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = MySQLModuleActivator.class, scope = SINGLETON, immediate = true)
public class MySQLModuleActivator {

    @Activate
    public void activate() throws BundleException {
        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
        } catch (SQLException e) {
            throw new BundleException("Could not register mysql driver", e);
        }
    }
}
