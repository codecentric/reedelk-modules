package com.esb.foonnel.rest;

import com.esb.foonnel.rest.commons.HostNamePortKey;
import com.esb.foonnel.rest.http.server.Server;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = ServerProvider.class, scope = SINGLETON)
public class ServerProvider {

    private Map<HostNamePortKey, Server> serverMap = new ConcurrentHashMap<>();

    public Server get(RestListenerConfiguration configuration) {
        HostNamePortKey key = new HostNamePortKey(configuration.getHostname(), configuration.getPort());
        if (!serverMap.containsKey(key)) {
            Server server = new Server(configuration);
            server.start();
            serverMap.put(key, server);
        }
        return serverMap.get(key);
    }

    void release(Server server) {
        if (server.emptyRoutes()) {
            server.stop();
            HostNamePortKey key = new HostNamePortKey(server.getHostname(), server.getPort());
            serverMap.remove(key);
        }
    }

}
