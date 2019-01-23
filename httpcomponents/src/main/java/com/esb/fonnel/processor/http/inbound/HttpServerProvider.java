package com.esb.fonnel.processor.http.inbound;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.MultiKeyMap;

class HttpServerProvider {

    private static MultiKeyMap<String,HttpServer> servers = MultiKeyMap.multiKeyMap(new HashedMap<>());

    static HttpServer get(int port, String bindAddress) {
        String portAsString = Integer.toString(port);
        if (!servers.containsKey(portAsString, bindAddress)) {
            HttpServer server = createAndStartServer(port, bindAddress);
            return server;
            //return servers.put(portAsString, bindAddress, server);
        }
        return servers.get(port, bindAddress);
    }

    private static HttpServer createAndStartServer(int port, String bindAddress) {
        HttpServer server = new HttpServer(port, bindAddress);
        server.start();
        return server;
    }


}
