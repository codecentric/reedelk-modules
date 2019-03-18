package com.esb.admin.console.dev;


import com.esb.api.exception.ESBException;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.http.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class DevAdminConsoleService {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Fork[] forks;

    private int port;
    private String bindAddress;
    private FtBasic server;
    private ExitStrategy exit = new ExitStrategy();

    DevAdminConsoleService(String bindAddress, int port, Fork... forks) {
        this.port = port;
        this.forks = forks;
        this.bindAddress = bindAddress;
    }

    void start() {
        TkFork tkFork = new TkFork(forks);
        executor.submit(new StartServer(exit, tkFork, bindAddress, port));
    }

    void stop() {
        if (server != null) exit.ready = true;
        shutdownExecutor();
    }

    private void shutdownExecutor() {
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class StartServer implements Runnable {

        private final int port;
        private final String bindAddress;

        private final Exit exit;
        private final TkFork routes;

        StartServer(Exit exit, TkFork routes, String bindAddress, int port) {
            this.exit = exit;
            this.port = port;
            this.routes = routes;
            this.bindAddress = bindAddress;
        }

        @Override
        public void run() {
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(bindAddress, port);
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.bind(socketAddress);

                Back bkSafe = new BkSafe(new BkBasic(routes));
                server = new FtBasic(bkSafe, serverSocket);
                server.start(exit);
            } catch (IOException e) {
                throw new ESBException(e);
            }
        }
    }

    class ExitStrategy implements Exit {

        private boolean ready = false;

        @Override
        public boolean ready() {
            return ready;
        }

    }

}
