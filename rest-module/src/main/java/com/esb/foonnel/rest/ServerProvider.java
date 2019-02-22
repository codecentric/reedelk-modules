package com.esb.foonnel.rest;

import com.esb.foonnel.rest.http.AbstractServerHandler;
import com.esb.foonnel.rest.http.Server;
import com.esb.foonnel.rest.http.ServerChannelInitializer;
import com.esb.foonnel.rest.http.ServerHandler;
import com.esb.foonnel.rest.route.Routes;
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
            Server server = newServer(port, hostname);
            server.start();
            serverMap.put(key, server);
        }
        return serverMap.get(key);
    }

    public void release(Server server) throws InterruptedException {
        if (server.emptyRoutes()) {
            server.stop();

            // TODO: Bleah!
            int port = server.getPort();
            String hostname = server.getHostname();
            KeyEntry key = new KeyEntry(hostname, port);
            serverMap.remove(key);
        }
    }

    private Server newServer(int port, String hostname) {
        Routes routes = new Routes();
        AbstractServerHandler serverHandler = new ServerHandler(routes);
        ServerChannelInitializer channelInitializer = new ServerChannelInitializer(serverHandler);
        return new Server(port, hostname, channelInitializer, routes);
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
