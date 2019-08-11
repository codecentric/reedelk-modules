package com.esb.rest.server;

import com.esb.rest.commons.HostNamePortKey;
import com.esb.rest.component.RestListenerConfiguration;
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
            serverMap.put(key, server);
        }
        return serverMap.get(key);
    }

    public void release(Server server) {
        // We stop  if and only if there are no
        // more routes associated to this server.
        if (server.hasEmptyRoutes()) {
            server.stop();
            serverMap.entrySet()
                    .stream()
                    .filter(key -> key.getValue() == server)
                    .findFirst()
                    .ifPresent(key -> serverMap.remove(key.getKey()));
        }
    }
}
