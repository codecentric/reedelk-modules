package com.esb.foonnel.admin.console.dev;


import com.esb.foonnel.api.FoonnelException;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.http.Exit;
import org.takes.http.FtBasic;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class DevAdminConsoleService {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Fork[] forks;

    private int port;
    private FtBasic server;
    private ExitStrategy exit = new ExitStrategy();

    DevAdminConsoleService(int port, Fork... forks) {
        this.port = port;
        this.forks = forks;
    }

    void start() {
        TkFork tkFork = new TkFork(forks);
        executor.submit(new StartServer(exit, tkFork, port));
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

        private final Exit exit;
        private final int port;
        private final TkFork routes;

        StartServer(Exit exit, TkFork routes, int port) {
            this.exit = exit;
            this.port = port;
            this.routes = routes;
        }

        @Override
        public void run() {
            try {
                server = new FtBasic(routes, port);
                server.start(exit);
            } catch (IOException e) {
                throw new FoonnelException(e);
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
