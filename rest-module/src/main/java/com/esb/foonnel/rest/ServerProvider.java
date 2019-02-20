package com.esb.foonnel.rest;

import com.esb.foonnel.rest.http.Server;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = ServerProvider.class, scope = SINGLETON)
public class ServerProvider {

    private Map<KeyEntry, Server> serverMap = new ConcurrentHashMap<>();

    public Server get(String hostname, int port) {
        KeyEntry key = new KeyEntry(hostname, port);
        if (!serverMap.containsKey(key)) {
            Server server = new Server(port, hostname);
            server.start();
            serverMap.put(key, server);
        }
        return serverMap.get(key);
    }

    public void release(Server server) throws InterruptedException {
        if (server.emptyRoutes()) {
            server.stop();
        }
    }

    private class KeyEntry {
        String hostname;
        int port;

        KeyEntry(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyEntry keyEntry = (KeyEntry) o;
            return port == keyEntry.port &&
                    hostname.equals(keyEntry.hostname);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hostname, port);
        }
    }

}
