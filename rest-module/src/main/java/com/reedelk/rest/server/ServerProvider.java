package com.reedelk.rest.server;

import com.reedelk.rest.commons.Defaults;
import com.reedelk.rest.commons.HostNamePortKey;
import com.reedelk.rest.configuration.listener.ListenerConfiguration;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = ServerProvider.class, scope = SINGLETON)
public class ServerProvider {

    private Map<HostNamePortKey, Server> serverMap = new ConcurrentHashMap<>();

    public Optional<Server> get(ListenerConfiguration configuration) {
        if (configuration == null) return Optional.empty();

        HostNamePortKey key = new HostNamePortKey(
                Defaults.RestListener.host(configuration.getHost()),
                Defaults.RestListener.port(configuration.getPort()));
        if (!serverMap.containsKey(key)) {
            Server server = new Server(configuration);
            serverMap.put(key, server);
        }
        Server server = serverMap.get(key);
        checkBasePathIsConsistent(configuration, server);
        return Optional.of(server);
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

    /**
     * If a server bound on a given hostname and port exists already, we
     * make sure that the server and the config have the same base path.
     * If they don't, it means that there are multiple configurations
     * defined on the same host and port but with different base paths.
     */
    private void checkBasePathIsConsistent(ListenerConfiguration configuration, Server server) {
        if (!Objects.equals(configuration.getBasePath(), server.getBasePath())) {
            throw new IllegalStateException("There are two server configurations " +
                    "on the same host and port with different base paths");
        }
    }
}
